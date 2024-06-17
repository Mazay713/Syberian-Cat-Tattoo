package com.example.tattooshka

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class Service_Details : Fragment() {
    private lateinit var serviceNameTextView: TextView
    private lateinit var serviceDescriptionTextView: TextView
    private lateinit var serviceCategoryTextView: TextView
    private lateinit var serviceImageView: ImageView

    companion object {
        private const val ARG_SERVICE_NAME = "serviceName"

        fun newInstance(serviceName: String): Service_Details {
            val fragment = Service_Details()
            val args = Bundle()
            args.putString(ARG_SERVICE_NAME, serviceName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_details, container, false)
        val serviceName = arguments?.getString(ARG_SERVICE_NAME)

        serviceNameTextView = view.findViewById(R.id.service_name)
        serviceDescriptionTextView = view.findViewById(R.id.service_description)
        serviceCategoryTextView = view.findViewById(R.id.service_category)
        serviceImageView = view.findViewById(R.id.service_image)

        // Загрузка данных об услуге из Firestore
        serviceName?.let { loadServiceDetails(it) }

        return view
    }

    private fun loadServiceDetails(serviceName: String) {
        FirebaseFirestore.getInstance().collection("services")
            .whereEqualTo("name", serviceName)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    serviceNameTextView.text = document.getString("name")
                    serviceDescriptionTextView.text = document.getString("description")
                    serviceCategoryTextView.text = document.getString("category")
                    // Загрузка изображения с помощью Picasso
                    document.getString("imageUrl")?.let { imageUrl ->
                        Picasso.get().load(imageUrl).into(serviceImageView)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Service_Details", "Error loading service details", exception)
            }
    }
}
