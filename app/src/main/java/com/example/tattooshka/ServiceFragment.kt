// ServiceFragment.kt
package com.example.tattooshka

import android.content.Context
import android.content.Intent
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
        searchResultsListView.setOnItemClickListener { adapterView, view, position, id ->
            val serviceName = adapterView.getItemAtPosition(position) as String
            viewModel.getServiceIdByName(serviceName).observe(viewLifecycleOwner) { serviceId ->
                Log.d("ServiceFragment", "Service ID: $serviceId")
                if (serviceId.isNotEmpty()) {
                    val intent = Intent(context, DetailActivity::class.java).apply {
                        putExtra("SERVICE_ID", serviceId as String)
                    }
                    startActivity(intent)
                } else {
                    Log.e("ServiceFragment", "Не найден ID для услуги: $serviceName")
                }
            }
        }
        return view
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchServicesByName(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchServicesByName(it) }
                return true
            }
        })
    }
    private fun observeServices() {
        viewModel.filteredServicesLiveData.observe(viewLifecycleOwner) { filteredServices ->
            searchResults.clear()
            searchResults.addAll(filteredServices)
            searchResultsAdapter.notifyDataSetChanged()
            searchResultsListView.visibility = if (filteredServices.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun updateUI(serviceDetailsList: List<ServiceViewModel.Service>) {
        val serviceInfoSet = serviceDetailsList.map { serviceDetails ->
            "Услуга: ${serviceDetails.name}, Категория: ${serviceDetails.category}"
        }.toSet() // Используем Set для уникальности

        searchResults.addAll(serviceInfoSet)
        searchResultsAdapter.notifyDataSetChanged()
        searchResultsListView.visibility = View.VISIBLE
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
    private val _filteredServicesLiveData = MutableLiveData<List<String>>()
    val filteredServicesLiveData: LiveData<List<String>> = _filteredServicesLiveData
    private val servicesCollection = FirebaseFirestore.getInstance().collection("services")
    data class Service(
        val name: String = "",
        val category: String = "",
        val price: Double = 0.0
    )
    fun searchServicesByName(query: String) {
        viewModelScope.launch {
            val filteredServices = getFilteredServicesByName(query)
            _filteredServicesLiveData.postValue(filteredServices)
        }
    }

    private suspend fun getFilteredServicesByName(query: String): List<String> {
        // Получаем список услуг напрямую из Firestore
        val servicesList = servicesCollection.get().await().documents.mapNotNull { it.getString("name") }
        // Фильтруем список услуг, используя запрос поиска
        return servicesList.filter { it.contains(query, ignoreCase = true) }
    }
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
                        if (!servicesList.contains(serviceName)) {
                            servicesList.add(serviceName)
                        }
                    }
                    document.getString("category")?.let { category ->
                        if (!categoriesList.contains(category)) {
                            categoriesList.add(category)
                        }
                    }
                }
                Log.d("ServiceViewModel", "Загруженные услуги: $servicesList")
                Log.d("ServiceViewModel", "Загруженные категории: $categoriesList")
            } catch (e: Exception) {
                Log.e("ServiceViewModel", "Error loading services", e)
            }
            _servicesLiveData.postValue(servicesList)
        }
    }


    fun searchServices(query: String) {
        viewModelScope.launch {
            val filteredServices = getFilteredServicesByQuery(query)
            Log.d("ServiceViewModel", "Отфильтрованные услуги: $filteredServices") // Добавьте логирование здесь
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
    fun getServiceIdByName(serviceName: String): LiveData<String> {
        val serviceIdLiveData = MutableLiveData<String>()
        viewModelScope.launch {
            try {
                val querySnapshot = servicesCollection
                    .whereEqualTo("name", serviceName)
                    .limit(1)
                    .get()
                    .await()

                val serviceId = if (querySnapshot.documents.isNotEmpty()) {
                    querySnapshot.documents[0].id // Получаем ID документа
                } else {
                    ""
                }
                serviceIdLiveData.postValue(serviceId)
            } catch (e: Exception) {
                Log.e("ServiceViewModel", "Ошибка при получении ID услуги: $serviceName", e)
            }
        }
        return serviceIdLiveData
    }


    fun getServiceDetailsLiveData(serviceName: String): LiveData<Service> {
        val liveData = MutableLiveData<Service>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val querySnapshot = servicesCollection
                    .whereEqualTo("name", serviceName)
                    .limit(1)
                    .get()
                    .await()

                val service = if (querySnapshot.documents.isNotEmpty()) {
                    querySnapshot.documents[0].toObject(Service::class.java) ?: Service()
                } else {
                    Service()
                }
                liveData.postValue(service)
            } catch (e: Exception) {
                Log.e("ServiceViewModel", "Ошибка при получении деталей услуги: $serviceName", e)
            }
        }
        return liveData
    }
}
