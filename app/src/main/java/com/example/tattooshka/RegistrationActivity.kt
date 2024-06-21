package com.example.tattooshka

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegistrationActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var editTextFirstName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var editTextEmail : EditText
    private lateinit var editTextPassword : EditText
    private lateinit var buttonSignUp: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        auth = Firebase.auth
        editTextFirstName = findViewById(R.id.editTextFirstName)
        editTextLastName = findViewById(R.id.editTextLastName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonSignUp = findViewById(R.id.buttonSignUp)
        buttonSignUp.setOnClickListener {
            createAccount()
        }
        }
        private val db = Firebase.firestore


data class User(
    val id: String? = null,
    val firstName : String? = null,
    val lastName: String? = null,
    val role : String? = null,
    val email:String? = null,
    val password: String? = null

)

    private fun createAccount () {
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()

        if (email.isBlank() || password.isBlank()){
              Toast(this).showCustomToast ("Не введен логин или пароль", this)

            return
        }
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){
            if (it.isSuccessful){
                val user = User(
                    auth.currentUser?.uid.toString(),
                    this.editTextFirstName.text.toString(),
                    this.editTextLastName.text.toString(),
                    "user",
                    email,
                    password
                )

                db.collection("users").document(auth.currentUser?.uid.toString()).set(user)
                    .addOnFailureListener {
                        Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                    }
                Toast(this).showCustomToast ("Регистрация прошла успешно", this)
                finish()
            }
            else{
                  Toast(this).showCustomToast ("ОШИБКА РЕГИСТРАЦИИ", this)


            }
        }


    }

}