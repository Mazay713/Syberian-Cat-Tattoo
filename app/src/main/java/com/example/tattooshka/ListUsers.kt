package com.example.tattooshka

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Модель данных пользователя
data class UserModel(
    val id: String,
    val lastName: String,
    val firstName: String,
    val email: String,
    val role: String,

)

// Адаптер для RecyclerView
class UserAdapter(
    private var users: MutableList<UserModel>,
    private val listener: OnUserClickListener
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    interface OnUserClickListener {
        fun onUserClick(user: UserModel)
        fun onUserDeleteClick(userId: String, position: Int)
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lastNameView: TextView = itemView.findViewById(R.id.lastNameTextView)
        private val firstNameView: TextView = itemView.findViewById(R.id.firstNameTextView)
        private val emailView: TextView = itemView.findViewById(R.id.emailTextView)
        private val roleView: TextView = itemView.findViewById(R.id.accessRoleTextView)
        private val deleteButton: Button = itemView.findViewById(R.id.buttonDeleteUser)

        fun bind(user: UserModel, position: Int) {
            lastNameView.text = user.lastName
            firstNameView.text = user.firstName
            emailView.text = user.email
            roleView.text = user.role

            itemView.setOnClickListener {
                listener.onUserClick(user)
            }

            deleteButton.setOnClickListener {
                listener.onUserDeleteClick(user.id, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user, position)
    }

    override fun getItemCount(): Int = users.size

    fun removeUserAt(position: Int) {
        users.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, users.size)
    }

    fun updateUserList(newUsers: List<UserModel>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}

// Активность для отображения списка пользователей
class ListUsers : AppCompatActivity(), UserAdapter.OnUserClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_users)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(mutableListOf(), this)
        recyclerView.adapter = adapter

        // Подписываемся на обновления в реальном времени из Firestore
        db.collection("users").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Toast.makeText(this, "Ошибка при загрузке пользователей: $e", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            val userList = snapshots?.map { document ->
                UserModel(
                    document.id,
                    document.getString("lastName") ?: "",
                    document.getString("firstName") ?: "",
                    document.getString("email") ?: "",
                    document.getString("role") ?: ""
                )
            } ?: listOf()

            adapter.updateUserList(userList)
        }
    }

    override fun onUserClick(user: UserModel) {
        Log.d("RecyclerView", "Item clicked: ${user.id}")
        val intent = Intent(this, UserEdit::class.java)
        intent.putExtra("userId", user.id)
        startActivity(intent)
    }

    override fun onUserDeleteClick(userId: String, position: Int) {
        deleteUser(userId, position)
    }

    private fun deleteUser(userId: String, position: Int) {
        db.collection("users").document(userId)
            .delete()
            .addOnSuccessListener {
                Log.d("ListUsers", "User successfully deleted!")
                adapter.removeUserAt(position)
            }
            .addOnFailureListener { e ->
                Log.w("ListUsers", "Error deleting user", e)
                Toast.makeText(this, "Ошибка при удалении пользователя: $e", Toast.LENGTH_LONG).show()
            }
    }
}
