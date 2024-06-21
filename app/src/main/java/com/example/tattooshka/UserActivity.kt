package com.example.tattooshka

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_services -> {
                    // Замените com.example.tattooshka.HomeFragment на фрагмент, который вы хотите отобразить на главной странице
                    replaceFragment(ServiceFragment())
                    true
                }
                R.id.navigation_workers -> {
                    // Замените com.example.tattooshka.FavoritesFragment на фрагмент для избранного
                    replaceFragment(WorkersFragment())
                    true
                }
                R.id.navigation_profile -> {
                    // Замените com.example.tattooshka.SearchFragment на фрагмент поиска
                    replaceFragment(ProfileFragment())
                    true
                }
                R.id.navigation_aboutUs ->{
                    replaceFragment(AboutUsFragment())
                    true
                }
                else -> false
            }
        }

        // Устанавливаем начальный фрагмент
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_services // ID начального элемента меню
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }
}

// Пример фрагмента для главной страницы
class HomeFragment : Fragment() {
    // Реализуйте ваш фрагмент здесь
}

// Пример фрагмента для избранного
class FavoritesFragment : Fragment() {
    // Реализуйте ваш фрагмент здесь
}

// Пример фрагмента для поиска
class SearchFragment : Fragment() {
    // Реализуйте ваш фрагмент здесь
}
