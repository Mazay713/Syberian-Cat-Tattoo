package com.example.tattooshka

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tattooshka.R
import com.google.firebase.firestore.FirebaseFirestore

class DetailActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Инициализация Firestore
        firestore = FirebaseFirestore.getInstance()

        // Получение serviceId из Intent
        val serviceId = intent.getStringExtra("SERVICE_ID")
        if (serviceId != null) {
            loadServiceData(serviceId)
        } else {
            // Обработка ошибки, если serviceId не был передан
            Log.e("DetailActivity", "Service ID is missing in the intent")
        }
    }

    private fun loadServiceData(serviceId: String) {
        // Загрузка данных об услуге из Firestore
        firestore.collection("services").document(serviceId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Обновление UI с информацией об услуге
                    val serviceNameTextView = findViewById<TextView>(R.id.service_name_text_view)
                    val serviceCategoryTextView = findViewById<TextView>(R.id.service_category_text_view)
                    val servicePriceTextView = findViewById<TextView>(R.id.service_price_text_view)
                    val backButton = findViewById<Button>(R.id.backButton)
                    backButton.setOnClickListener {
                        finish()
                    }
                    serviceNameTextView.text = document.getString("name")
                    serviceCategoryTextView.text = "Категория: ${document.getString("category")}"
                    servicePriceTextView.text = "Цена(Руб.): ${document.getDouble("price").toString()}"
                } else {
                    Log.e("DetailActivity", "No service found with ID: $serviceId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DetailActivity", "Error getting service details: ", exception)
            }
    }
}
