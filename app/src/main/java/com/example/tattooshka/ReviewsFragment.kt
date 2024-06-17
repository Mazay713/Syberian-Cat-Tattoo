package com.example.tattooshka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tattooshka.databinding.FragmentReviewsBinding
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

}

