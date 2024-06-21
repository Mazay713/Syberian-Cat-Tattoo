package com.example.tattooshka

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.tattooshka.databinding.FragmentReviewsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ReviewsAdapter(
    var reviews: MutableList<Review>
) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val commentTextView: TextView = itemView.findViewById(R.id.tvComment)

        fun bind(review: Review) {
            ratingBar.rating = review.rating.toFloat()
            commentTextView.text = review.comment
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size
}

class ReviewsFragment : Fragment() {

    private lateinit var binding: FragmentReviewsBinding
    private lateinit var workerId: String
    private lateinit var reviewRepository: ReviewRepository
    private lateinit var avatarImageView: ImageView
    private lateinit var firstNameTextView: TextView
    private lateinit var secondNameTextView: TextView
    private lateinit var experienceTextView: TextView
    private lateinit var photosGridView: GridView
    private val PICK_IMAGE_REQUEST = 71
    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestoreReference = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        avatarImageView = binding.avatarImageView
        firstNameTextView = binding.firstNameTextView
        secondNameTextView = binding.secondNameTextView
        experienceTextView = binding.experienceTextView
        photosGridView = binding.photosGridView
        loadUserData()
        // Получаем ID сотрудника из аргументов фрагмента
        workerId = requireArguments().getString("workerId") ?: ""

        // Инициализируем репозиторий для работы с отзывами
        reviewRepository = ReviewRepository(requireContext())

        // Настраиваем RecyclerView для отображения отзывов
        setupRecyclerView()

        // Обработчик нажатия на кнопку "Отправить"
        binding.btnSubmit.setOnClickListener {
            val rating = binding.ratingBar.rating
            val comment = binding.etComment.text.toString()
            val review = Review(workerId, rating.toDouble(), comment)

            // Отправляем отзыв на сервер с помощью репозитория
            reviewRepository.addReview(review)
            binding.ratingBar.rating = 0f
            binding.etComment.text.clear()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = ReviewsAdapter(mutableListOf())
        binding.recyclerView.adapter = adapter

        // Загружаем отзывы для выбранного работника из Firestore
        reviewRepository.getReviews(workerId, adapter)
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
        workerId = requireArguments().getString("workerId") ?: ""
        // Загружаем данные работника из коллекции 'users' по workerId
        firestoreReference.collection("users").document(workerId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val firstName = document.getString("firstName") ?: ""
                    val secondName = document.getString("lastName") ?: ""
                    val experience = document.getString("experience") ?: ""

                    // Обновляем интерфейс с информацией о работнике
                    firstNameTextView.text = firstName
                    secondNameTextView.text = secondName
                    experienceTextView.text = experience

                    // Загрузка аватара и фотографий работника
                    val avatarRef = storageReference.child("avatars/$workerId.jpg")
                    avatarRef.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(this@ReviewsFragment).load(uri).into(avatarImageView)
                    }

                    // Предполагается, что фотографии работника хранятся в папке 'photos' с ID работника
                    val photosRef = storageReference.child("photos/$workerId")
                    photosRef.listAll()
                        .addOnSuccessListener { listResult ->
                            val imageUrls = ArrayList<String>()
                            listResult.items.forEach { storageRef ->
                                storageRef.downloadUrl.addOnSuccessListener { uri ->
                                    imageUrls.add(uri.toString())
                                    if (imageUrls.size == listResult.items.size) {
                                        val adapter = ImageAdapter(requireContext(), imageUrls)
                                        photosGridView.adapter = adapter
                                    }
                                }
                            }
                        }
                } else {
                    Log.e("WorkerActivity", "Документ не найден")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("WorkerActivity", "Ошибка запроса данных работника", exception)
            }
    }

}

