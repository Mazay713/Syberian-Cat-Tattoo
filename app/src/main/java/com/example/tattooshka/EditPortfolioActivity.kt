package com.example.tattooshka

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class EditPortfolioActivity : AppCompatActivity() {

    private lateinit var avatarImageView: ImageView
    private lateinit var firstNameEditText: EditText
    private lateinit var secondNameEditText: EditText
    private lateinit var experienceEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var uploadAvatarButton: Button
    private lateinit var storageReference: StorageReference
    private lateinit var photosGridView: GridView
    private lateinit var addPhotoButton: Button
    private val pickImageForPortfolioLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let { uri ->
                uploadImageToStorage(uri)
            }
        }
    }
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
        photosGridView = findViewById(R.id.photosGridView)

        avatarImageView = findViewById(R.id.avatarImageView) ?: throw NullPointerException("View with ID avatarImageView not found")
        firstNameEditText = findViewById(R.id.firstNameEditText) ?: throw NullPointerException("View with ID firstNameEditText not found")
        secondNameEditText = findViewById(R.id.secondNameEditText) ?: throw NullPointerException("View with ID secondNameEditText not found")
        experienceEditText = findViewById(R.id.experienceEditText) ?: throw NullPointerException("View with ID experienceEditText not found")
        saveButton = findViewById(R.id.saveButton) ?: throw NullPointerException("View with ID saveButton not found")
        addPhotoButton = findViewById(R.id.addPhotoButton)
        loadUserData()
        saveButton.setOnClickListener {
            saveProfile()
            finish()
        }
        photosGridView.setOnItemLongClickListener { _, _, position, _ ->
            // Создание контекстного меню или диалогового окна для подтверждения удаления
            AlertDialog.Builder(this)
                .setTitle("Удаление фотографии")
                .setMessage("Вы уверены, что хотите удалить эту фотографию?")
                .setPositiveButton("Удалить") { dialog, _ ->
                    val adapter = photosGridView.adapter as ImageAdapter
                    adapter.removeItem(position)
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена", null)
                .show()
            true // Возвращает true, чтобы указать, что событие было обработано
        }
        addPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            pickImageForPortfolioLauncher.launch(intent)
        }
        uploadAvatarButton = findViewById(R.id.uploadAvatarButton)
        storageReference = FirebaseStorage.getInstance().reference

        uploadAvatarButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }
    }
    private fun uploadImageToStorage(imageUri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val photoRef = storageReference.child("photos/$userId/${UUID.randomUUID()}.jpg")

        photoRef.putFile(imageUri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    savePhotoUrlToFirestore(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка при загрузке изображения", Toast.LENGTH_SHORT).show()
            }
    }
    private fun savePhotoUrlToFirestore(photoUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val portfolioRef = firestoreDB.collection("portfolios").document(userId)
        val photoId = UUID.randomUUID().toString()

        portfolioRef.collection("photos").document(photoId)
            .set(mapOf("url" to photoUrl))
            .addOnSuccessListener {
                Toast.makeText(this, "Фото добавлено в портфолио", Toast.LENGTH_SHORT).show()
                // Обновите GridView с помощью Glide
                updatePhotosGridView()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка при добавлении фото в портфолио", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updatePhotosGridView() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val portfolioRef = firestoreDB.collection("portfolios").document(userId)

        portfolioRef.collection("photos").get()
            .addOnSuccessListener { querySnapshot ->
                val imageUrls = querySnapshot.documents.mapNotNull { it.getString("url") }
                val adapter = ImageAdapter(this, imageUrls)
                val photosGridView: GridView = findViewById(R.id.photosGridView)
                photosGridView.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка при обновлении портфолио", Toast.LENGTH_SHORT).show()
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
                        val photosRef = storageReference.child("photos/$userId")
                        photosRef.listAll()
                            .addOnSuccessListener { listResult ->
                                val imageUrls = ArrayList<String>()
                                listResult.items.forEach { storageRef ->
                                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                                        imageUrls.add(uri.toString())
                                        if (imageUrls.size == listResult.items.size) {
                                            val adapter = ImageAdapter(this@EditPortfolioActivity, imageUrls)
                                            photosGridView.adapter = adapter
                                        }
                                    }.addOnFailureListener {
                                        Toast.makeText(this, "Ошибка при загрузке фотографий", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                avatarRef.downloadUrl.addOnSuccessListener { uri ->
                                    Glide.with(this@EditPortfolioActivity).load(uri)
                                        .into(avatarImageView)
                                }
                                // Загрузите другие данные, если они есть
                            }
                    }else {
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
    fun deletePhotoFromPortfolio(photoUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // Найдите ссылку на фотографию в Firebase Storage и удалите её
        val photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl)
        photoRef.delete()
            .addOnSuccessListener {
                // Фотография удалена из Storage, теперь удаляем её из Firestore
                val portfolioRef = firestoreDB.collection("portfolios").document(userId)
                portfolioRef.collection("photos").whereEqualTo("url", photoUrl).get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            portfolioRef.collection("photos").document(document.id).delete()
                        }
                        // Обновите GridView после удаления фотографии
                        updatePhotosGridView()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка при удалении фотографии", Toast.LENGTH_SHORT).show()
            }
    }
    private fun saveProfile() {
        val userUpdates = hashMapOf<String, Any>(
            "firstName" to firstNameEditText.text.toString(),
            "lastName" to secondNameEditText.text.toString(),
            "experience" to "Стаж: " + experienceEditText.text.toString()
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
class ImageAdapter(private val context: Context, private val imageUrls: List<String>) : BaseAdapter() {

    override fun getCount(): Int = imageUrls.size
    override fun getItem(position: Int): Any = imageUrls[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView: ImageView = (convertView as? ImageView) ?: ImageView(context).apply {
            layoutParams = AbsListView.LayoutParams(370, 370)
            setPadding(10, 10, 10, 10) // Установите желаемый отступ
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        Glide.with(context).load(imageUrls[position]).into(imageView)
        return imageView

    }
    fun removeItem(position: Int) {
        val photoUrl = getItem(position) as String
        (context as? EditPortfolioActivity)?.deletePhotoFromPortfolio(photoUrl)
        (imageUrls as MutableList).removeAt(position)
        notifyDataSetChanged()
    }

}

