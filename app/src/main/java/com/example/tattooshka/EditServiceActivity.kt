package com.example.tattooshka

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditServiceActivity : AppCompatActivity() {

    private lateinit var editTextServiceName: EditText
    private lateinit var editTextServiceCategory: EditText
    private lateinit var editTextServicePrice: EditText
    private lateinit var buttonSave: Button
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_service)

        editTextServiceName = findViewById(R.id.editTextServiceName)
        editTextServiceCategory = findViewById(R.id.editTextServiceCategory)
        editTextServicePrice = findViewById(R.id.editTextServicePrice)
        buttonSave = findViewById(R.id.buttonSave)

        val serviceId = intent.getStringExtra("SERVICE_ID")
        if (serviceId != null) {
            db.collection("services").document(serviceId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val serviceItem = documentSnapshot.toObject(ServiceItem::class.java)
                        editTextServiceName.setText(serviceItem?.name)
                        editTextServiceCategory.setText(serviceItem?.category)
                        editTextServicePrice.setText(serviceItem?.price.toString())
                    } else {
                        // Обработка ситуации, когда документ не найден
                    }
                }
                .addOnFailureListener {
                    // Обработка ошибки загрузки данных
                }
        }

        buttonSave.setOnClickListener {
            val name = editTextServiceName.text.toString()
            val category = editTextServiceCategory.text.toString()
            val price = editTextServicePrice.text.toString().toDoubleOrNull() ?: 0.0

            if (serviceId != null && serviceId.isNotEmpty()) {
                val serviceItem = ServiceItem(serviceId, name, category, price)
                db.collection("services").document(serviceId).set(serviceItem)
                    .addOnSuccessListener {
                        // Обработка успешного обновления услуги
                    }
                    .addOnFailureListener {
                        // Обработка ошибки обновления услуги
                    }
            }
            finish()
        }
    }
}
