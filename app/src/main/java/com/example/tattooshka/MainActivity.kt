package com.example.tattooshka

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var editTextPassword: EditText
    private lateinit var editTextEmail: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView: TextView = findViewById(R.id.textViewSignUp)

        textView.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        auth = FirebaseAuth.getInstance()

        val SignInbtn: Button = findViewById(R.id.buttonSignIn)
        SignInbtn.setOnClickListener {
            login()

        }
    }
    private val db = Firebase.firestore
    private fun login() {
        val email = editTextEmail.text.toString()
        val pass = editTextPassword.text.toString()
            if (email.isBlank() || pass.isBlank()) {
                Toast(this).showCustomToast("Логин или пароль не введен", this)
            } else {

                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
                    if (it.isSuccessful) {
//                        Toast(this).showCustomToast("Успешная авторизация", this)
//                        val intent = Intent(this, com.example.tattooshka.UserActivity::class.java)
//                        startActivity(intent)
                        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
                        if(currentUserUid != null) {
                            db.collection("users").document(currentUserUid).get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()){
                                        val user = document.getString("role")
                                        Log.d(TAG, "Role is $document")
                                        when(user){
                                            "admin" -> startActivity(Intent(this, AdminActivity::class.java))
                                            "user" -> startActivity(Intent(this, UserActivity::class.java))
//                                            "worker" -> startActivity(Intent(this, WorkerActivity::class.java))
                                        }
                                    } else {
                                        Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT)
                                    }

//                                if (user != null && user.role == "admin") {
//                                    intent = Intent(this, com.example.tattooshka.com.example.tattooshka.com.example.tattooshka.com.example.tattooshka.com.example.tattooshka.com.example.tattooshka.com.example.tattooshka.AdminActivity::class.java)
//                                    startActivity(intent)
//                                } else if(user!=null && user.role == "user") {
//                                    intent = Intent(this, com.example.tattooshka.UserActivity::class.java)
//                                    startActivity(intent)
//                                } else if(user!= null && user.role == "worker"){
//                                    intent = Intent(this, com.example.tattooshka.WorkerActivity::class.java)
//                                    startActivity(intent)
//                                }
                            }.addOnFailureListener {
                                Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast(this).showCustomToast("Ошибка авторизации", this)
                    }
                }

                    .addOnFailureListener { exception ->
                        Log.d(TAG, "get failed with ", exception)
                    }

            }
        }
    }







