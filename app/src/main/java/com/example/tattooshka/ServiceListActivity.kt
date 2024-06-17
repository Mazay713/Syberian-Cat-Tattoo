package com.example.tattooshka

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class ServiceListActivity : AppCompatActivity(), ServiceAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddService: FloatingActionButton
    private val db = FirebaseFirestore.getInstance()
    private val serviceList = mutableListOf<ServiceItem>()
    private lateinit var adapter: ServiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_list)

        recyclerView = findViewById(R.id.recyclerViewServices)
        fabAddService = findViewById(R.id.fabAddService)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ServiceAdapter(serviceList, this)
        recyclerView.adapter = adapter

        // Подписываемся на обновления в реальном времени из Firestore
        db.collection("services").addSnapshotListener { snapshots, e ->
            if (e != null) {
                // Обработка ошибки
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                val newServiceList = mutableListOf<ServiceItem>()
                for (document in snapshots.documents) {
                    val service = document.toObject(ServiceItem::class.java)?.apply {
                        id = document.id // Добавляем id из документа Firestore
                    }
                    service?.let { newServiceList.add(it) }
                }
                serviceList.clear()
                serviceList.addAll(newServiceList)
                adapter.notifyDataSetChanged()
            }
        }

        fabAddService.setOnClickListener {
            val intent = Intent(this, Service::class.java)
            startActivity(intent)
        }
    }

    override fun onItemClick(serviceItem: ServiceItem) {
        val intent = Intent(this, EditServiceActivity::class.java)
        intent.putExtra("SERVICE_ID", serviceItem.id)
        startActivity(intent)
    }
}

data class ServiceItem(
    var id: String = "", // Добавлено поле id
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
     // Добавлено поле role
)

class ServiceAdapter(private val services: MutableList<ServiceItem>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(serviceItem: ServiceItem)
    }

    class ServiceViewHolder(val view: View, val adapter: ServiceAdapter) : RecyclerView.ViewHolder(view) {
        fun bind(service: ServiceItem, listener: OnItemClickListener) {
            view.findViewById<TextView>(R.id.textViewServiceName).text = service.name
            view.findViewById<TextView>(R.id.textViewServiceCategory).text = service.category
            view.findViewById<TextView>(R.id.textViewServicePrice).text = service.price.toString()
            view.findViewById<Button>(R.id.buttonDeleteService).setOnClickListener {
                adapter.removeAt(adapterPosition)
            }
            view.setOnClickListener {
                listener.onItemClick(service)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(services[position], listener)
    }

    override fun getItemCount() = services.size

    fun removeAt(position: Int) {
        val serviceId = services[position].id
        FirebaseFirestore.getInstance().collection("services").document(serviceId).delete()
            .addOnSuccessListener {
                services.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, services.size)
            }
            .addOnFailureListener {
                // Обработка ошибки удаления
            }
    }
}
