package com.example.homey

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.homey.auth.AuthManager
import com.example.homey.ui.AddRealEstateActivity
import com.example.homey.ui.LoginActivity
import com.example.homey.ui.SettingsFragment

class MainPage : AppCompatActivity() {

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        // Initialize AuthManager
        authManager = AuthManager()

        // Kiểm tra trạng thái đăng nhập
        if (!authManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Load the default fragment
        loadFragment(HomeFragment())

        val navHome = findViewById<LinearLayout>(R.id.nav_home)
        navHome.setOnClickListener {
            loadFragment(HomeFragment())
        }

        val navSettings = findViewById<LinearLayout>(R.id.nav_settings)
        navSettings.setOnClickListener {
            loadFragment(SettingsFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
