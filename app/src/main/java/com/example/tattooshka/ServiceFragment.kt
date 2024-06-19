// ServiceFragment.kt
package com.example.tattooshka

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ServiceFragment : Fragment() {
    private lateinit var viewModel: ServiceViewModel
    private lateinit var searchView: SearchView
    private lateinit var gridView: GridView
    private lateinit var adapter: ImageTextAdapter
    private var servicesList = arrayListOf("Черно-белые татуировки", "Цветные татуировки", "Пирсинг")
    private var imagesList = arrayListOf(R.drawable.blacktattoo, R.drawable.colortattoo, R.drawable.pircing)
    private lateinit var searchResultsListView: ListView
    private lateinit var searchResultsAdapter: ArrayAdapter<String>
    private val searchResults = mutableListOf<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service, container, false)

        gridView = view.findViewById(R.id.photo_grid)
        adapter = ImageTextAdapter(requireContext(), servicesList, imagesList) { serviceName ->
            showServiceDetails(serviceName)
        }
        searchResultsListView = view.findViewById(R.id.search_results_list)
        searchResultsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, searchResults)
        searchResultsListView.adapter = searchResultsAdapter
        gridView.adapter = adapter
        viewModel = ViewModelProvider(this).get(ServiceViewModel::class.java)
        searchView = view.findViewById(R.id.search_service)
        setupSearch()
        observeServices()
        return view
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchServices(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchServices(it) }
                return true
            }
        })
    }

    private fun observeServices() {
        viewModel.filteredServicesLiveData.observe(viewLifecycleOwner) { filteredServices ->
            searchResults.clear()
            filteredServices.forEach { serviceName ->
                viewModel.getServiceDetails(serviceName).observe(viewLifecycleOwner) { serviceItem ->
                    // Убедитесь, что используете правильный экземпляр объекта 'serviceItem'
                    val displayString = "Услуга: ${serviceItem.name}, Категория: ${serviceItem.category}, Цена: ${serviceItem.price}"
                    if (!searchResults.contains(displayString)) {
                        searchResults.add(displayString)
                    }
                    searchResultsAdapter.notifyDataSetChanged()
                }
            }
            searchResultsListView.visibility = if (searchResults.isEmpty()) View.GONE else View.VISIBLE
        }
    }





    private fun showServiceDetails(categoryName: String) {
        // Создание нового фрагмента с услугами по категории
        val servicesByCategoryFragment = ServicesByCategoryFragment().apply {
            arguments = Bundle().apply {
                putString("categoryName", categoryName)
            }
        }

        // Запуск транзакции фрагмента для замены текущего фрагмента в контейнере
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, servicesByCategoryFragment)
            addToBackStack(null) // Добавление транзакции в стек возврата
            commit() // Выполнение транзакции
        }
    }

}

// ImageTextAdapter.kt
class ImageTextAdapter(
    private val context: Context,
    private var services: ArrayList<String>,
    private var images: ArrayList<Int>,
    private val onItemClicked: (String) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int {
        return services.size
    }

    override fun getItem(position: Int): Any {
        return services[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = convertView ?: inflater.inflate(R.layout.grid_single, parent, false)

        val image = images[position]
        val text = services[position]

        val imageView = view.findViewById<ImageView>(R.id.grid_image)
        val textView = view.findViewById<TextView>(R.id.grid_text)

        imageView.setImageResource(image)
        textView.text = text

        view.setOnClickListener {
            onItemClicked(services[position])
        }

        return view
    }
}
class ServiceViewModel : ViewModel() {
    private val _servicesLiveData = MutableLiveData<List<String>>()
    val servicesLiveData: LiveData<List<String>> = _servicesLiveData
    private val _filteredServicesLiveData = MutableLiveData<List<String>>()
    val filteredServicesLiveData: LiveData<List<String>> = _filteredServicesLiveData
    private val servicesCollection = FirebaseFirestore.getInstance().collection("services")

    init {
        loadServices()
    }

    private fun loadServices() {
        viewModelScope.launch(Dispatchers.IO) {
            val servicesList = mutableListOf<String>()
            val categoriesList = mutableListOf<String>()
            try {
                val querySnapshot = servicesCollection.get().await()
                for (document in querySnapshot.documents) {
                    document.getString("name")?.let { serviceName ->
                        servicesList.add(serviceName)
                    }
                    document.getString("category")?.let { category ->
                        if (!categoriesList.contains(category)) {
                            categoriesList.add(category)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ServiceViewModel", "Error loading services", e)
            }
            _servicesLiveData.postValue(servicesList)
        }
    }
    fun searchServices(query: String) {
        viewModelScope.launch {
            val filteredServices = getFilteredServicesByQuery(query)
            _filteredServicesLiveData.postValue(filteredServices)
        }
    }
    private suspend fun getFilteredServicesByQuery(query: String): List<String> {
        return withContext(Dispatchers.IO) {
            _servicesLiveData.value?.filter { service ->
                service.contains(query, ignoreCase = true)
            } ?: listOf()
        }
    }
    fun filterServicesByCategory(category: String) {
        viewModelScope.launch {
            val filteredServices = getFilteredServicesByCategory(category)
            _filteredServicesLiveData.postValue(filteredServices)
        }
    }

    private suspend fun getFilteredServicesByCategory(category: String): List<String> {
        return withContext(Dispatchers.IO) {
            _servicesLiveData.value?.filter { service ->
                getCategoryForService(service) == category
            } ?: listOf()
        }
    }
    private suspend fun getCategoryForService(serviceName: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = servicesCollection
                    .whereEqualTo("category", serviceName)
                    .limit(1) // Поскольку имя услуги уникально, нам нужен только один документ
                    .get()
                    .await()

                if (querySnapshot.documents.isNotEmpty()) {
                    querySnapshot.documents[0].getString("category") ?: "Неизвестная категория"
                } else {
                    "Категория не найдена"
                }
            } catch (e: Exception) {
                Log.e("ServiceViewModel", "Ошибка при получении категории для услуги: $serviceName", e)
                "Ошибка при запросе"
            }
        }
    }
    fun getServiceDetails(serviceName: String): LiveData<Service> {
        val serviceDetailsLiveData = MutableLiveData<Service>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val querySnapshot = servicesCollection
                    .whereEqualTo("name", serviceName)
                    .limit(1)
                    .get()
                    .await()

                val service = if (querySnapshot.documents.isNotEmpty()) {
                    querySnapshot.documents[0].toObject(Service::class.java)
                } else {
                    Service() // Возвращаем пустой объект Service, если услуга не найдена.
                }
                serviceDetailsLiveData.postValue(service)
            } catch (e: Exception) {
                Log.e("ServiceViewModel", "Ошибка при получении деталей услуги: $serviceName", e)
            }
        }
        return serviceDetailsLiveData
    }
}
