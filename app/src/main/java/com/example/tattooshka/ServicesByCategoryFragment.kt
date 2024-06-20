package com.example.tattooshka

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import java.util.Locale

enum class SortType {
    ALPHABET_ASCENDING,
    ALPHABET_DESCENDING,
    PRICE_ASCENDING,
    PRICE_DESCENDING
}

@Suppress("DEPRECATION")
class ServicesByCategoryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServicesAdapter
    private var servicesList = mutableListOf<Service2>()
    private lateinit var spinnerSort: Spinner

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
        spinnerSort = view.findViewById(R.id.spinner_sort)
        // Настройка Spinner для выбора сортировки
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSort.adapter = adapter
        }

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val sortType = parent.getItemAtPosition(position).toString()
                loadServicesByCategory(categoryName, sortType)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Ничего не делать
            }
        }

        return view
    }
    val sortTypeMap = mapOf(
        "По алфавиту по возрастанию" to SortType.ALPHABET_ASCENDING,
        "По алфавиту по убыванию" to SortType.ALPHABET_DESCENDING,
        "По цене по возрастанию" to SortType.PRICE_ASCENDING,
        "По цене по убыванию" to SortType.PRICE_DESCENDING
    )
    private fun loadServicesByCategory(categoryName: String, sortTypeString: String) {
        val sortType = sortTypeMap[sortTypeString] ?: SortType.ALPHABET_ASCENDING
        val query = FirebaseFirestore.getInstance().collection("services")
            .whereEqualTo("category", categoryName)

        val sortedQuery = when (sortType) {
            SortType.ALPHABET_ASCENDING -> query.orderBy("name", Query.Direction.ASCENDING)
            SortType.ALPHABET_DESCENDING -> query.orderBy("name", Query.Direction.DESCENDING)
            SortType.PRICE_ASCENDING -> query.orderBy("price", Query.Direction.ASCENDING)
            SortType.PRICE_DESCENDING -> query.orderBy("price", Query.Direction.DESCENDING)
        }
        Log.d("Firestore", "Тип сортировки: $sortType")
        sortedQuery.get()
            .addOnSuccessListener { documents ->
                Log.d("Firestore", "Документы успешно получены")
                servicesList.clear()
                for (document in documents) {
                    val service = document.toObject<Service2>()
                    Log.d("Firestore", "Услуга: ${service.name}, Цена: ${service.price}")
                    servicesList.add(service)
                }
                activity?.runOnUiThread {
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Ошибка при получении документов: ${exception.message}")
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
    val id: String? = null,
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0
    // Дополнительные поля, которые могут быть вам нужны
)
