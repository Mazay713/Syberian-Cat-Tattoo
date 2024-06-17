package com.example.tattooshka
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val buttonWorkers = findViewById<Button>(R.id.buttonWorkers)
        val buttonServices = findViewById<Button>(R.id.buttonServices)

        // Обработчик нажатия для кнопки "Работники"
        buttonWorkers.setOnClickListener {
            // Здесь должен быть код для перехода на страницу "Работники"
            val intent = Intent(this, ListUsers::class.java)
            startActivity(intent)
        }

        // Обработчик нажатия для кнопки "Услуги"
        buttonServices.setOnClickListener {
            // Здесь должен быть код для перехода на страницу "Услуги"
            val intent = Intent(this, ServiceListActivity::class.java)
            startActivity(intent)
        }
    }
}