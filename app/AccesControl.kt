package com.example.tattooshka
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccessControl {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun checkAccessLevel(activity: BaseActivity) {
        val userId = auth.currentUser?.uid

        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            if (document != null) {
                val userLevel = document.getString("access_level")

                when (userLevel) {
                    "admin" -> activity.navigateToAdminActivity()
                    "user" -> activity.navigateToUserActivity()
                    "employee" -> activity.navigateToEmployeeActivity()
                    else -> activity.showAccessDenied()
                }
            } else {
                activity.showAccessDenied()
            }
        }.addOnFailureListener {
            activity.showErrorMessage()
        }
    }
}