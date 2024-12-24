package com.example.homey

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.homey.ui.AddRealEstateActivity
import com.example.homey.ui.HomeFragment
import com.example.homey.ui.FavoriteFragment
import com.example.homey.ui.SettingsFragment

class MainPage : AppCompatActivity() {
    private lateinit var homeFragment: HomeFragment
    private lateinit var favoriteFragment: FavoriteFragment
    private lateinit var settingsFragment: SettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)


        // Initialize fragments
        homeFragment = HomeFragment()
        favoriteFragment = FavoriteFragment()
        settingsFragment = SettingsFragment()

        // Add fragments to the FragmentManager
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, homeFragment, "HOME_FRAGMENT")
            .add(R.id.fragment_container, favoriteFragment, "FAVORITE_FRAGMENT")
            .add(R.id.fragment_container, settingsFragment, "SETTINGS_FRAGMENT")
            .hide(favoriteFragment)
            .hide(settingsFragment)
            .commit()

        val navHome = findViewById<LinearLayout>(R.id.nav_home)
        navHome.setOnClickListener {
            loadFragment(homeFragment)
        }

        val navFavorite = findViewById<LinearLayout>(R.id.nav_favorite)
        navFavorite.setOnClickListener {
            loadFragment(favoriteFragment)
        }

        val navSettings = findViewById<LinearLayout>(R.id.nav_settings)
        navSettings.setOnClickListener {
            loadFragment(settingsFragment)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(homeFragment)
            .hide(favoriteFragment)
            .hide(settingsFragment)
            .show(fragment)
            .commit()
    }
}