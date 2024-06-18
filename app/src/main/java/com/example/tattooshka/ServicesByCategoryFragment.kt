package com.example.tattooshka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class ServicesByCategoryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServicesAdapter
    private var servicesList = mutableListOf<Service2>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_services_by_category, container, false)
        recyclerView = view.findViewById(R.id.services_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ServicesAdapter(servicesList)
        recyclerView.adapter = adapter
        val categoryName = arguments?.getString("categoryName") ?: ""
        val textViewServiceCategory: TextView = view.findViewById(R.id.ServiceCategory)
        textViewServiceCategory.text = categoryName
        loadServicesByCategory(categoryName)
        return view

    }

    private fun loadServicesByCategory(categoryName: String) {
        FirebaseFirestore.getInstance().collection("services")
            .whereEqualTo("category", categoryName)
            .get()
            .addOnSuccessListener { documents ->
                servicesList.clear()
                for (document in documents) {
                    val service = document.toObject<Service2>()
                    servicesList.add(service)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Обработка ошибки
            }
    }
}

class ServicesAdapter(private val servicesList: MutableList<Service2>) : RecyclerView.Adapter<ServicesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewName: TextView = view.findViewById(R.id.service_name)
        val textViewPrice: TextView = view.findViewById(R.id.service_price)
        // Дополнительные элементы UI, если они вам нужны
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = servicesList[position]
        holder.textViewName.text = service.name
        holder.textViewPrice.text = service.price.toString()

        // Заполнение дополнительных данных, если они есть
    }

    override fun getItemCount() = servicesList.size
}

data class Service2(
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0
    // Дополнительные поля, которые могут быть вам нужны
)
