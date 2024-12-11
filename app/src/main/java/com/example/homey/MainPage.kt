package com.example.homey

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.homey.ui.AddRealEstateActivity
import com.example.homey.ui.SettingsFragment

class MainPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        // Load the default fragment
        loadFragment(HomeFragment())

        // Nav bar logic
        val navAddEstate = findViewById<ImageButton>(R.id.nav_add_estate)
        navAddEstate.setOnClickListener {
            val intent = Intent(this, AddRealEstateActivity::class.java)
            startActivity(intent)
        }

        val navHome = findViewById<ImageButton>(R.id.nav_home)
        navHome.setOnClickListener {
            loadFragment(HomeFragment())
        }

        val navSettings = findViewById<ImageButton>(R.id.nav_categories)
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