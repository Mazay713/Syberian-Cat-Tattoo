package com.example.tattooshka

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ReviewRepository(private val context: Context) {
    private val db = Firebase.firestore

    fun addReview(review: Review) {
        // Сохраняем отзыв на сервере
        val reviewsRef = db.collection("reviews")
        reviewsRef.document("${review.workerId}_${System.currentTimeMillis()}").set(review)
            .addOnSuccessListener {
                Toast.makeText(context, "Отзыв успешно отправлен", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                showErrorToast(e)
            }
    }

    fun getReviews(workerId: String, adapter: ReviewsAdapter) {
        // Загружаем отзывы для выбранного работника из Firestore
        db.collection("reviews")
            .whereEqualTo("workerId", workerId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    showErrorToast(e)
                    return@addSnapshotListener
                }

                val reviewList = snapshots?.toObjects(Review::class.java) ?: listOf()
                adapter.reviews.clear()
                adapter.reviews.addAll(reviewList)
                adapter.notifyDataSetChanged()
            }
    }

    private fun showErrorToast(e: Exception) {
        Toast.makeText(context, "Ошибка при загрузке отзывов: $e", Toast.LENGTH_LONG).show()
    }
}
