package com.example.homey.ui

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homey.R
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.title = "Forgot Password"

        // Allow go back
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Interactivity
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val resetPasswordButton = findViewById<Button>(R.id.resetPasswordButton)
        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isEmpty()) {
                showAlertDialog("Error", "Please enter your email address.")
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showAlertDialog("Success", "An email has been sent to you to reset your password.")
                    } else {
                        showAlertDialog("Error", "Failed to send the email. Please check your email address.")
                    }
                }
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK", { dialog, which ->
            dialog.dismiss()
            finish()
        })
        val alertDialog = builder.create()
        alertDialog.show()
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}