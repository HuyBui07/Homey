package com.example.homey.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.homey.R
import com.example.homey.data.repository.UserRepository
import com.bumptech.glide.Glide

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

        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        
        val avatarImageView = view.findViewById<ImageView>(R.id.avatarImageView)
        val usernameTextView = view.findViewById<TextView>(R.id.usernameTextView)
        val emailTextView = view.findViewById<TextView>(R.id.emailTextView)
        val phoneTextView = view.findViewById<TextView>(R.id.phoneTextView)

        val imageUrl = UserRepository.getInstance().getImageUrl()
        if (imageUrl != null) {
            Glide.with(this)
                .load(imageUrl)
                .into(avatarImageView)
        }

        val username = UserRepository.getInstance().getUsername()
        if (username != null) {
            usernameTextView.text = username
        }

        val email = UserRepository.getInstance().getEmail()
        if (email != null) {
            emailTextView.text = email
        }

        val phone = UserRepository.getInstance().getPhoneNumber()
        if (phone != null) {
            phoneTextView.text = phone
        }

        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // udpate the view
                val newImageUrl = UserRepository.getInstance().getImageUrl()
                if (newImageUrl != null) {
                    Glide.with(this)
                        .load(newImageUrl)
                        .into(avatarImageView)
                }

                val newUsername = UserRepository.getInstance().getUsername()
                if (newUsername != null) {
                    usernameTextView.text = newUsername
                }

                val newEmail = UserRepository.getInstance().getEmail()
                if (newEmail != null) {
                    emailTextView.text = newEmail
                }

                val newPhone = UserRepository.getInstance().getPhoneNumber()
                if (newPhone != null) {
                    phoneTextView.text = newPhone
                }
            }
        }

        val editFrameLayout = view.findViewById<FrameLayout>(R.id.editFrameLayout)
        editFrameLayout.setOnClickListener {
            val intent = Intent(activity, EditAccountActivity::class.java)
            startForResult.launch(intent)
        }

        val myEstatesButton = view.findViewById<LinearLayout>(R.id.myEstatesLinearLayout)
        myEstatesButton.setOnClickListener {
            val intent = Intent(activity, MyEstatesActivity::class.java)
            startActivity(intent)
        }

        val logoutLinearLayout = view.findViewById<LinearLayout>(R.id.logOutLinearLayout)
        logoutLinearLayout.setOnClickListener {
            UserRepository.getInstance().logoutUser()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }
}