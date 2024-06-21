package com.example.tattooshka

// Импорты
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class WorkerActivity : AppCompatActivity() {
    private lateinit var avatarImageView: ImageView
    private lateinit var firstNameTextView: TextView
    private lateinit var secondNameTextView: TextView
    private lateinit var experienceTextView: TextView
    private lateinit var photosGridView: GridView
    private lateinit var buttonEditPortfolio: Button
    private lateinit var buttonRefresh: ImageButton
    private val PICK_IMAGE_REQUEST = 71
    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestoreReference = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker)
        loadUserData()
        avatarImageView = findViewById(R.id.avatarImageView)
        firstNameTextView = findViewById(R.id.firstNameTextView)
        secondNameTextView = findViewById(R.id.secondNameTextView)
        experienceTextView = findViewById(R.id.experienceTextView)
        photosGridView = findViewById(R.id.photosGridView)
        buttonEditPortfolio = findViewById(R.id.editPortfolioButton)
        buttonEditPortfolio.setOnClickListener {
            val intent = Intent(this, EditPortfolioActivity::class.java)
            startActivity(intent)

        }
        buttonRefresh = findViewById(R.id.buttonRefresh)
        buttonRefresh.setOnClickListener {
            loadUserData()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            uploadImageToFirebase(imageUri)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            val fileName = "avatar_${FirebaseAuth.getInstance().currentUser?.uid}.jpg"
            val imageRef = storageReference.child("images/$fileName")
            imageRef.putFile(imageUri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        updateAvatarUrlInFirestore(uri.toString())
                    }
                }
                .addOnFailureListener {
                    Log.e("WorkerActivity", "Ошибка загрузки изображения", it)
                }
        }
    }

    private fun updateAvatarUrlInFirestore(avatarUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestoreReference.collection("users").document(userId)
            .update("avatarUrl", avatarUrl)
            .addOnSuccessListener {
                Glide.with(this)
                    .load(avatarUrl)
                    .error(R.drawable.profile) // Запасное изображение
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("WorkerActivity", "Ошибка загрузки изображения", e)
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.i("WorkerActivity", "Изображение загружено")
                            return false
                        }
                    })
                    .into(avatarImageView)
            }
            .addOnFailureListener {
                Log.e("WorkerActivity", "Ошибка обновления URL аватара", it)
            }
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestoreReference.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val firstName = document.getString("firstName") ?: ""
                    val secondName = document.getString("lastName") ?: ""
                    val experience = document.getString("experience") ?: ""
                    val avatarRef = storageReference.child("avatars/$userId.jpg")
                    val photosRef = storageReference.child("photos/$userId")
                    photosRef.listAll()
                        .addOnSuccessListener { listResult ->
                            val imageUrls = ArrayList<String>()
                            listResult.items.forEach { storageRef ->
                                storageRef.downloadUrl.addOnSuccessListener { uri ->
                                    imageUrls.add(uri.toString())
                                    if (imageUrls.size == listResult.items.size) {
                                        val adapter =
                                            ImageAdapter(this@WorkerActivity, imageUrls)
                                        photosGridView.adapter = adapter
                                    }
                                }.addOnFailureListener {
                                    Toast.makeText(this, "Ошибка при загрузке фотографий", Toast.LENGTH_SHORT).show()
                                }
                            }
                            avatarRef.downloadUrl.addOnSuccessListener { uri ->
                                Glide.with(this@WorkerActivity).load(uri).into(avatarImageView)
                            }
                            firstNameTextView.text = firstName
                            secondNameTextView.text = secondName
                            experienceTextView.text = "Стаж: $experience" // Обновлено здесь
                        }
                } else {
                    Log.e("WorkerActivity", "Документ не найден")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("WorkerActivity", "Ошибка запроса данных пользователя", exception)
            }
    }

}
