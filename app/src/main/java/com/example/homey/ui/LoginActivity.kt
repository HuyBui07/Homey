package com.example.homey.ui

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homey.MainPage
import com.example.homey.R
import com.example.homey.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Hide action bar
        supportActionBar?.hide()

        // Interactivity
        val logInTextView = findViewById<TextView>(R.id.logInTextView)
        val text = "Sign Up / Login"

        val spannable = SpannableString(text)
        spannable.setSpan(StyleSpan(Typeface.BOLD), 9, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(RelativeSizeSpan(1.4f), 9, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        logInTextView.text = spannable

        logInTextView.setOnClickListener {
            // Navigate to the sign up screen
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val emailEditText = findViewById<TextView>(R.id.emailEditText)
        val passwordEditText = findViewById<TextView>(R.id.passwordEditText)
        val progressBar = findViewById<FrameLayout>(R.id.loadingOverlay)

        val loginButton = findViewById<Button>(R.id.logInButton)
        loginButton.setOnClickListener {
            progressBar.visibility = FrameLayout.VISIBLE

            // Login user
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                UserRepository.getInstance().loginUser(email, password) { success ->
                    if (success) {
                        // Check if email is verified
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null && user.isEmailVerified) {
                            // Navigate to the home screen
                            progressBar.visibility = FrameLayout.GONE
                            val intent = Intent(this, MainPage::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            overridePendingTransition(0, 0)
                        } else {
                            // Show error message
                            progressBar.visibility = FrameLayout.GONE
                            showAlertDialog("Error", "Please verify your email address")
                        }
                    } else {
                        // Show error message
                        progressBar.visibility = FrameLayout.GONE
                        showAlertDialog("Error", "Invalid email or password")
                    }
                }
            } else {
                // Show error message
                progressBar.visibility = FrameLayout.GONE
                showAlertDialog("Error", "Please fill in all fields")
            }
        }

        val forgotPasswordTextView = findViewById<TextView>(R.id.forgotPasswordTextView)
        forgotPasswordTextView.setOnClickListener {
            // Navigate to the forgot password screen
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }
}