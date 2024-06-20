//package com.example.tattooshka
//
//import android.content.Intent
//import android.os.Bundle
//import android.os.Parcel
//import android.os.Parcelable
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.database.DatabaseReference
//
//class WorkerActivity() : AppCompatActivity(), Parcelable {
//
//    private lateinit var database: DatabaseReference
//
//    constructor(parcel: Parcel) : this() {
//        // Инициализация переменных из Parcel, если это необходимо
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_worker)
//
//        // Инициализация Firebase Database
//        database = FirebaseDatabase.getInstance().getReference()
//
//        val textViewWorkerName: TextView = findViewById(R.id.textViewWorkerName)
//        val textViewWorkerExperience: TextView = findViewById(R.id.textViewWorkerExperience)
//        val imageViewWorkerAvatar: ImageView = findViewById(R.id.imageViewWorkerAvatar)
//        val buttonEditPortfolio: Button = findViewById(R.id.buttonEditPortfolio)
//
//        // Получение данных из Firebase
//        database.child("workers").child("workerId").addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val workerName = dataSnapshot.child("name").getValue(String::class.java)
//                val workerExperience = dataSnapshot.child("experience").getValue(String::class.java)
//                // Здесь вы можете добавить код для загрузки изображения аватара из Firebase Storage, если необходимо
//
//                textViewWorkerName.text = workerName ?: ""
//                textViewWorkerExperience.text = workerExperience ?: ""
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Обработка ошибок
//            }
//        })
//
//        // Обработчик нажатия кнопки для редактирования портфолио
//        buttonEditPortfolio.setOnClickListener {
//            val intent = Intent(this, EditPortfolioActivity::class.java)
//            startActivity(intent)
//        }
//    }
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        // Запись переменных в Parcel, если это необходимо
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<WorkerActivity> {
//        override fun createFromParcel(parcel: Parcel): WorkerActivity {
//            return WorkerActivity(parcel)
//        }
//
//        override fun newArray(size: Int): Array<WorkerActivity?> {
//            return arrayOfNulls(size)
//        }
//    }
//}
