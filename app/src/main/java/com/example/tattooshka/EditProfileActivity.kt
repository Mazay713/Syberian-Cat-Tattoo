package com.example.tattooshka

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.Calendar

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etBirthDate: EditText
    private lateinit var ivAvatar: ImageView
    private lateinit var btnSave: Button
    private lateinit var btnUploadImage: Button
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        etBirthDate = findViewById(R.id.etBirthDate)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etPhone = findViewById(R.id.etPhone)
        etBirthDate = findViewById(R.id.etBirthDate)
        ivAvatar = findViewById(R.id.ivAvatar)
        btnSave = findViewById(R.id.btnSave)
        btnUploadImage = findViewById(R.id.btnUploadImage)
        // Инициализация компонентов UI и другие операции...

        etBirthDate.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private val ddmmyyyy = "ДДММГГГГ"
            private val cal = Calendar.getInstance()

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() != current) {
                    var clean = s.toString().replace("[^\\d.]".toRegex(), "")
                    val cleanC = current.replace("[^\\d.]".toRegex(), "")

                    val cl = clean.length
                    var sel = cl
                    for (i in 2..cl step 2) {
                        sel++
                    }
                    // Fix for pressing delete next to a forward slash
                    if (clean == cleanC) sel--

                    if (clean.length < 8) {
                        clean += ddmmyyyy.substring(clean.length)
                    } else {
                        if (clean.length >= 10) {
                            try {
                                var day = Integer.parseInt(clean.substring(0, 2))
                                var mon = Integer.parseInt(clean.substring(3, 5))
                                var year = Integer.parseInt(clean.substring(6, 10))

                                mon = if (mon < 1) 1 else if (mon > 12) 12 else mon
                                cal.set(Calendar.MONTH, mon - 1)
                                year = if (year < 1900) 1900 else if (year > 2100) 2100 else year
                                cal.set(Calendar.YEAR, year)
                                day = if (day > cal.getActualMaximum(Calendar.DATE)) cal.getActualMaximum(Calendar.DATE) else day
                                clean = String.format("%02d.%02d.%04d", day, mon, year)
                            } catch (e: NumberFormatException) {
                                // Обработка ошибки, возможно установка значения по умолчанию или вывод сообщения
                            }
                        } else {
                            // Обработка случая, когда строка слишком короткая для разбора даты
                        }
                    }

                    sel = if (sel < 0) 0 else sel
                    current = clean
                    etBirthDate.setText(current)
                    etBirthDate.setSelection(if (sel < current.length) sel else current.length)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?) {}
        })
        loadUserData()

        btnSave.setOnClickListener {
            saveUserData()

        }

        btnUploadImage.setOnClickListener {
            openImageChooser()
        }
    }

    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Выберите аватарку"), 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            ivAvatar.setImageURI(imageUri)
        }
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        userId?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        etFirstName.setText(document.getString("firstName"))
                        etLastName.setText(document.getString("lastName"))
                        etPhone.setText(document.getString("phone"))
                        etBirthDate.setText(document.getString("birthDay"))
                        // Загрузка изображения пользователя, если оно есть
                        val storageRef = FirebaseStorage.getInstance().reference
                        val imageRef = storageRef.child("images/$userId/avatar.jpg")
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            Glide.with(this@EditProfileActivity).load(uri).into(ivAvatar)
                        }.addOnFailureListener {
                            // Обработка ошибок загрузки изображения
                        }
                    } else {
                        // Документ не найден
                    }
                }
                .addOnFailureListener { exception ->
                    // Обработка ошибок получения документа
                }
        }
    }

    private fun saveUserData() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val storageRef = FirebaseStorage.getInstance().reference

        val user = hashMapOf(
            "email" to currentUser?.email,
            "id" to userId,
            "firstName" to etFirstName.text.toString(),
            "lastName" to etLastName.text.toString(),
            "phone" to etPhone.text.toString(),
            "birthDay" to etBirthDate.text.toString(),
            "role" to "user"

        )

        userId?.let {
            db.collection("users").document(it)
                .set(user)
                .addOnSuccessListener {

                    imageUri?.let { uri ->
                        val imageRef = storageRef.child("images/$userId/avatar.jpg")
                        imageRef.putFile(uri)
                            .addOnSuccessListener {
                                // Успешная загрузка аватарки
                            }
                            .addOnFailureListener {
                                // Обработка ошибки загрузки
                            }
                    }
                    finish()
                }
                .addOnFailureListener {
                    // Обработка ошибки сохранения данных
                }
        }

    }
}
