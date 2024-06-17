package com.example.tattooshka
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Button
import com.google.firebase.firestore.FirebaseFirestore

class Service : AppCompatActivity() {
    private lateinit var editTextServiceName: EditText
    private lateinit var editTextServiceCategory: EditText
    private lateinit var editTextServicePrice: EditText
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        editTextServiceName = findViewById(R.id.editTextServiceName)
        editTextServiceCategory = findViewById(R.id.editTextServiceCategory)
        editTextServicePrice = findViewById(R.id.editTextServicePrice)

        val buttonSave = findViewById<Button>(R.id.buttonSaveService)
        buttonSave.setOnClickListener {
            val name = editTextServiceName.text.toString()
            val category = editTextServiceCategory.text.toString()
            val priceText = editTextServicePrice.text.toString()
            val price = priceText.toDoubleOrNull() ?: 0.0

            // Создание нового ServiceItem с уникальным ID
            val newServiceItem = ServiceItem(name = name, category = category, price = price)
            val newDocument = db.collection("services").document() // Создание нового документа с уникальным ID
            newServiceItem.id = newDocument.id // Присваивание ID новому ServiceItem

            newDocument.set(newServiceItem)
                .addOnSuccessListener {
                    // Обработка успешного сохранения услуги
                    Log.d(TAG, "DocumentSnapshot added with ID: ${newServiceItem.id}")
                }
                .addOnFailureListener { e ->
                    // Обработка ошибки сохранения услуги
                    Log.w(TAG, "Error adding document", e)
                }
            finish()
        }
    }
}
