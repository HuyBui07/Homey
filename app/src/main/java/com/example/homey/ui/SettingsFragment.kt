package com.example.homey.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.homey.MainPage
import com.example.homey.R

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Hide action bar
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        // Interactivity
        val myEstatesButton = view.findViewById<LinearLayout>(R.id.myEstatesLinearLayout)
        myEstatesButton.setOnClickListener {
            val intent = Intent(activity, MyEstatesActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}