package com.example.tattooshka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
data class PortfolioItem(val id: String, val imageUrl: String)



class WorkerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: PortfolioAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker)

//        recyclerView = findViewById(R.id.recyclerView)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        adapter = PortfolioAdapter { item -> /* обработчик нажатия */ }
//        recyclerView.adapter = adapter

        // Загрузка данных портфолио
        loadPortfolioItems()
    }

    private fun loadPortfolioItems() {
        db.collection("portfolio").get()
            .addOnSuccessListener { documents ->
                val items = documents.map { doc ->
                    PortfolioItem(doc.id, doc.getString("imageUrl") ?: "")
                }
//                adapter.setItems(items)
            }
            .addOnFailureListener { exception ->
                // Обработка ошибки
            }
    }

    // Функции для удаления и редактирования портфолио
    fun deletePortfolioItem(item: PortfolioItem) {
        db.collection("portfolio").document(item.id).delete()
            .addOnSuccessListener {
                // Успешное удаление
            }
            .addOnFailureListener {
                // Обработка ошибки
            }
    }

    // Функция для редактирования элемента портфолио
    // Функция для редактирования элемента портфолио
    fun editPortfolioItem(portfolioItemId: String) {
        val db = FirebaseFirestore.getInstance()
        val portfolioItemRef = db.collection("portfolio").document(portfolioItemId)

        // Получение данных элемента портфолио
        portfolioItemRef.get().addOnSuccessListener { documentSnapshot ->
            val portfolioItem = documentSnapshot.toObject(PortfolioItem::class.java)
            portfolioItem?.let { item ->
                // Показать диалоговое окно с текущими данными элемента для редактирования
                showEditPortfolioItemDialog(item)
            }
        }.addOnFailureListener {
            // Обработка ошибки получения данных
        }
    }

    // Функция для вызова диалога редактирования элемента портфолио
    fun showEditPortfolioItemDialog(item: PortfolioItem) {
        // ... Код диалогового окна для редактирования ...
    }

    // Обновление элемента портфолио в Firestore
    fun updatePortfolioItem(updatedItem: PortfolioItem) {
        val db = FirebaseFirestore.getInstance()
        db.collection("portfolio").document(updatedItem.id)
            .set(updatedItem)
            .addOnSuccessListener {
                // Обработка успешного обновления
            }
            .addOnFailureListener {
                // Обработка ошибки обновления
            }
    }


}
//class PortfolioAdapter(private val onItemClick: (PortfolioItem) -> Unit) :
//    RecyclerView.Adapter<PortfolioAdapter.ViewHolder>() {

//    var items: List<PortfolioItem> = listOf()

//    fun setItems(newItems: List<PortfolioItem>) {
//        items = newItems
//        notifyDataSetChanged()
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_portfolio, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val item = items[position]
//        Glide.with(holder.itemView.context).load(item.imageUrl).into(holder.imageView)
//        holder.itemView.setOnClickListener { onItemClick(item) }
//    }
//
//    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }
//}
