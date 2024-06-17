package com.example.tattooshka

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class UserEdit : AppCompatActivity() {

    private lateinit var userRoleSpinner: Spinner
    private lateinit var updateUserButton: Button
    private lateinit var textViewName: TextView
    private lateinit var textViewEmail: TextView
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_edit)

        userRoleSpinner = findViewById(R.id.userRoleSpinner)
        updateUserButton = findViewById(R.id.buttonUpdateUser)
        textViewName = findViewById(R.id.TextUserName)
        textViewEmail = findViewById(R.id.TextUserEmail)
        // Предполагаем, что userId передается в эту Activity, например, через Intent
        val userId = intent.getStringExtra("userId") ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    textViewName.text = document.getString("firstName")
                    textViewEmail.text = document.getString("email")
                } else {
                    // Обработка ситуации, когда документ не найден
                }
            }
            .addOnFailureListener { exception ->
                // Обработка ошибки загрузки данных
            }
        // Установка слушателя кликов напрямую в коде Kotlin
        updateUserButton.setOnClickListener {
            updateUser(userId)
            finish()
        }
    }

    private fun updateUser(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val usersCollectionRef = db.collection("users")

        // Получение выбранной роли из Spinner
        val selectedRole = userRoleSpinner.selectedItem.toString()

        // Обновление роли пользователя в Firestore
        usersCollectionRef.document(userId).update("role", selectedRole).addOnSuccessListener {
            // Обработка успешного обновления
            // Например, показываем сообщение об успешном обновлении
        }.addOnFailureListener {
            // Обработка ошибки обновления
            // Например, показываем сообщение об ошибке
        }
    }
}

// Класс User и RegistrationActivity должны быть определены в вашем проекте
