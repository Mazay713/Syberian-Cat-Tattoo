// ServiceDetailActivity.kt
package com.example.tattooshka

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class ServiceDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_detail)

        val serviceName = intent.getStringExtra("SERVICE_NAME")
        val serviceCategory = intent.getStringExtra("SERVICE_CATEGORY")
        val servicePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0)

        findViewById<TextView>(R.id.textViewServiceNameDetail).text = serviceName
        findViewById<TextView>(R.id.textViewServiceCategoryDetail).text = serviceCategory
        findViewById<TextView>(R.id.textViewServicePriceDetail).text = servicePrice.toString()
        // Внутри класса ServiceDetailActivity

        val buttonOpenService = findViewById<Button>(R.id.buttonOpenService)
        buttonOpenService.setOnClickListener {
            // Здесь должен быть код для открытия экрана Service
            val intent = Intent(this, Service::class.java)
            startActivity(intent)
        }

    }
}
