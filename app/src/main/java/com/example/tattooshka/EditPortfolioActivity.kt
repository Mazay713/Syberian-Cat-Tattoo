package com.example.tattooshka

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class EditPortfolioActivity : AppCompatActivity() {

    private lateinit var avatarImageView: ImageView
    private lateinit var firstNameEditText: EditText
    private lateinit var secondNameEditText: EditText
    private lateinit var experienceEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var uploadAvatarButton: Button
    private lateinit var storageReference: StorageReference
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let { uri ->
                uploadImageToFirebase(uri)
            }
        }
    }
    private val firestoreDB = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_portfolio) // Убедитесь, что здесь указано правильное имя макета

        avatarImageView = findViewById(R.id.avatarImageView) ?: throw NullPointerException("View with ID avatarImageView not found")
        firstNameEditText = findViewById(R.id.firstNameEditText) ?: throw NullPointerException("View with ID firstNameEditText not found")
        secondNameEditText = findViewById(R.id.secondNameEditText) ?: throw NullPointerException("View with ID secondNameEditText not found")
        experienceEditText = findViewById(R.id.experienceEditText) ?: throw NullPointerException("View with ID experienceEditText not found")
        saveButton = findViewById(R.id.saveButton) ?: throw NullPointerException("View with ID saveButton not found")
        loadUserData()
        saveButton.setOnClickListener {
            saveProfile()
            finish()
        }
        uploadAvatarButton = findViewById(R.id.uploadAvatarButton)
        storageReference = FirebaseStorage.getInstance().reference

        uploadAvatarButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }
    }
    private fun uploadImageToFirebase(imageUri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val avatarRef = storageReference.child("avatars/$userId.jpg")
            avatarRef.putFile(imageUri)
                .addOnSuccessListener {
                    Toast.makeText(this, "Аватар успешно загружен", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Ошибка при загрузке аватара", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
        }
    }
    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestoreDB.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.data
                        firstNameEditText.setText(user?.get("firstName")?.toString())
                        secondNameEditText.setText(user?.get("lastName")?.toString())
                        experienceEditText.setText(user?.get("experience")?.toString())
                        val avatarRef = storageReference.child("avatars/$userId.jpg")
                        avatarRef.downloadUrl.addOnSuccessListener { uri ->
                            Glide.with(this@EditPortfolioActivity).load(uri).into(avatarImageView)
                        }
                        // Загрузите другие данные, если они есть
                    } else {
                        Toast.makeText(this, "Данные пользователя не найдены", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Ошибка при загрузке данных: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
        }
    }
    private fun saveProfile() {
        val userUpdates = hashMapOf<String, Any>(
            "firstName" to firstNameEditText.text.toString(),
            "lastName" to secondNameEditText.text.toString(),
            "experience" to experienceEditText.text.toString()
            // Добавьте другие поля здесь, если необходимо
        )

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestoreDB.collection("users").document(userId)
                .update(userUpdates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Профиль успешно обновлён", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Ошибка при обновлении профиля: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
        }
    }

}
