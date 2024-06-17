package com.example.tattooshka

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {
    private lateinit var ivAvatar: ImageView
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()
        ivAvatar = view.findViewById(R.id.ivAvatar)
        loadUserData()

        // Находим кнопку редактирования профиля и устанавливаем обработчик нажатия
        view.findViewById<Button>(R.id.button_edit_profile).setOnClickListener {
            // Здесь код для перехода на экран редактирования профиля
            // Например, если у вас есть Activity под названием EditProfileActivity
            val editProfileIntent = Intent(activity, EditProfileActivity::class.java)
            startActivity(editProfileIntent)
        }

        // Находим кнопку выхода из аккаунта и устанавливаем обработчик нажатия
        view.findViewById<Button>(R.id.button_logout).setOnClickListener {
            // Выход пользователя из аккаунта
            FirebaseAuth.getInstance().signOut()
            // Переход на экран входа
            val loginIntent = Intent(activity, MainActivity::class.java)
            startActivity(loginIntent)
            activity?.finish()
        }
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        userId?.let {
            db.collection("users").document(it)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val email = document.getString("email") ?: ""
                        val firstName = document.getString("firstName") ?: ""
                        val lastName = document.getString("lastName") ?: ""
                        val phone = document.getString("phone")?:"Не указан"
                        val birthDay = document.getString("birthDay")?:"Не указан"
                        val storageRef = FirebaseStorage.getInstance().reference
                        val imageRef = storageRef.child("images/$userId/avatar.jpg")
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            Glide.with(this@ProfileFragment).load(uri).into(ivAvatar)
                        }
                        updateUI(email, firstName, lastName, phone, birthDay)
                    } else {
                        Log.d("UserProfileFragment", "Документ не найден")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("UserProfileFragment", "Ошибка получения документа: ", exception)
                }
        }
    }

    private fun updateUI(email: String, firstName: String, lastName: String, phone:String, birthDay: String) {
        view?.findViewById<TextView>(R.id.tvEmail)?.text = email
        view?.findViewById<TextView>(R.id.tvFirstName)?.text = firstName
        view?.findViewById<TextView>(R.id.tvLastName)?.text = lastName
        view?.findViewById<TextView>(R.id.tvPhoneNumber)?.text = phone
        view?.findViewById<TextView>(R.id.tvBirthDate)?.text = birthDay
    }
}
