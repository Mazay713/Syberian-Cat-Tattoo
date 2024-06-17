package com.example.tattooshka

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class Worker(
    val id: String,
    val lastName: String,
    val firstName: String,
    val email: String,
)
data class Review(
    val workerId: String = "",
    val rating: Double = 0.0,
    val comment: String = ""
)


class WorkerAdapter(
    private var workers: MutableList<Worker>,
    private val context: Context,
    private val listener: WorkersFragment
) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    interface OnWorkerClickListener {
        fun onWorkerClick(worker: Worker)
    }

    inner class WorkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lastNameView: TextView = itemView.findViewById(R.id.tvLastName)
        private val firstNameView: TextView = itemView.findViewById(R.id.tvFirstName)
        private val emailView: TextView = itemView.findViewById(R.id.tvEmail)

        fun bind(worker: Worker, position: Int) {
            lastNameView.text = worker.lastName
            firstNameView.text = worker.firstName
            emailView.text = worker.email

            itemView.setOnClickListener {
                listener.onWorkerClick(worker)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.worker_item, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val worker = workers[position]
        holder.bind(worker, position)
    }

    override fun getItemCount(): Int = workers.size

    fun updateWorkerList(newWorkers: List<Worker>) {
        workers.clear()
        workers.addAll(newWorkers)
        notifyDataSetChanged()
    }
}

class WorkersFragment : Fragment(), WorkerAdapter.OnWorkerClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WorkerAdapter
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workers, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = WorkerAdapter(mutableListOf(), requireContext(), this)
        recyclerView.adapter = adapter

        // Подписываемся на обновления в реальном времени из Firestore
        db.collection("users").whereEqualTo("role", "worker").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Toast.makeText(context, "Ошибка при загрузке работников: $e", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            val workerList = snapshots?.map { document ->
                Worker(
                    document.id,
                    document.getString("firstName") ?: "",
                    document.getString("lastName") ?: "",
                    document.getString("email") ?: ""
                )

            } ?: listOf()

            adapter.updateWorkerList(workerList)
        }

        return view
    }

    override fun onWorkerClick(worker: Worker) {
        Log.d("RecyclerView", "Item clicked: ${worker.id}")
        // Реализуйте логику перехода на страницу редактирования работника
        val fragment = ReviewsFragment()
        val bundle = Bundle()
        bundle.putString("workerId", worker.id)
        fragment.arguments = bundle
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

}







