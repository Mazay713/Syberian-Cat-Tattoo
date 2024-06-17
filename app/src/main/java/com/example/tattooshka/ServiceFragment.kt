// ServiceFragment.kt
package com.example.tattooshka

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
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
    private lateinit var servicesTextView: TextView
    private lateinit var gridView: GridView
    private lateinit var adapter: ImageTextAdapter
    private var servicesList = arrayListOf("Черно-белые татуировки", "Цветные татуировки", "Пирсинг")
    private var imagesList = arrayListOf(R.drawable.blacktattoo, R.drawable.colortattoo, R.drawable.pircing)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service, container, false)

        gridView = view.findViewById(R.id.photo_grid)
        adapter = ImageTextAdapter(requireContext(), servicesList, imagesList) { serviceName ->
            showServiceDetails(serviceName)
        }
        gridView.adapter = adapter
        viewModel = ViewModelProvider(this).get(ServiceViewModel::class.java)
        searchView = view.findViewById(R.id.search_service)
        servicesTextView = view.findViewById(R.id.services_text_view)
//        setupSearch()
        observeServices()
        return view
    }
//
//    private fun setupSearch() {
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                query?.let { viewModel.searchServices(it) }
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                newText?.let { viewModel.searchServices(it) }
//                return true
//            }
//        })
//    }

    private fun observeServices() {
        viewModel.servicesLiveData.observe(viewLifecycleOwner, { services ->
            if (services.isEmpty()) {
                servicesTextView.text = "Нет доступных услуг"
            } else {
                servicesTextView.text = services.joinToString("\n")
            }
        })
    }

    private fun showServiceDetails(serviceName: String) {
        val serviceDetailsFragment = Service_Details.newInstance(serviceName)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, serviceDetailsFragment)
            .addToBackStack(null)
            .commit()
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
    private val _categoriesLiveData = MutableLiveData<List<String>>()
    val categoriesLiveData: LiveData<List<String>> = _categoriesLiveData

    private val servicesCollection = FirebaseFirestore.getInstance().collection("services")

    init {
        loadServices()
    }

    private fun loadServices() {
        viewModelScope.launch {
            val services = withContext(Dispatchers.IO) {
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
                _categoriesLiveData.postValue(categoriesList)
                servicesList
            }
            _servicesLiveData.postValue(services)
        }
    }

    fun filterServicesByCategory(category: String) {
        viewModelScope.launch {
            val filteredServices = withContext(Dispatchers.IO) {
                _servicesLiveData.value?.filter { service ->
                    getCategoryForService(service) == category
                }
            }
            _servicesLiveData.postValue(filteredServices ?: listOf())
        }
    }

    private suspend fun getCategoryForService(serviceName: String): String {
        var category = "Неизвестная категория"
        val querySnapshot = servicesCollection.whereEqualTo("category", serviceName).get().await()
        for (document in querySnapshot.documents) {
            category = document.getString("category") ?: "Неизвестная категория"
            break // Поскольку имя услуги уникально, нам нужен только первый документ
        }
        return category
    }
}