package com.example.homey.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.homey.R
import com.example.homey.auth.AuthManager
import com.example.homey.ui.LoginActivity

class SettingsFragment : Fragment() {

    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize AuthManager
        authManager = AuthManager()

        // Handle window insets for padding
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Hide action bar
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        // Navigate to My Estates
        val myEstatesButton = view.findViewById<LinearLayout>(R.id.myEstatesLinearLayout)
        myEstatesButton.setOnClickListener {
            val intent = Intent(activity, MyEstatesActivity::class.java)
            startActivity(intent)
        }

        // Logout functionality
        val logoutButton = view.findViewById<LinearLayout>(R.id.logoutLinearLayout)
        logoutButton.setOnClickListener {
            authManager.logout()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }

        return view
    }
}
