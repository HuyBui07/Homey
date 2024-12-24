package com.example.homey.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.SpannableString
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homey.R
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.homey.MainPage
import com.example.homey.data.repository.UserRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resumeWithException
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseAuth


class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Hide action bar
        supportActionBar?.hide()

        // Interactivity
        val signUpTextView = findViewById<TextView>(R.id.signUpTextView)
        val text = "Sign Up / Login"

        val spannable = SpannableString(text)
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            9,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        ) // "Sign Up /" in bold
        spannable.setSpan(RelativeSizeSpan(1.4f), 0, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        signUpTextView.text = spannable

        signUpTextView.setOnClickListener {
            // Navigate to the sign up screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        // Sign up information interactivity
        val avatarFrameLayout = findViewById<FrameLayout>(R.id.avatarFrameLayout)
        val avatarImageView = findViewById<ImageView>(R.id.avatarImage)
        val plusImageView = findViewById<ImageView>(R.id.plusImage)

        val selectImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        Glide.with(this)
                            .load(selectedImageUri)
                            .transform(CircleCrop())
                            .into(avatarImageView)
                        avatarImageView.tag = selectedImageUri
                        plusImageView.visibility = ImageView.GONE
                    }
                }
            }

        avatarFrameLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            selectImageLauncher.launch(intent)
        }

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val userNameEditText = findViewById<EditText>(R.id.userNameEditText)
        val phoneNumberEditText = findViewById<EditText>(R.id.phoneNumberEditText)

        val signUpButton = findViewById<TextView>(R.id.signUpButton)
        val progressBar = findViewById<FrameLayout>(R.id.loadingOverlay)
        signUpButton.setOnClickListener {
            progressBar.visibility = FrameLayout.VISIBLE

            Handler(Looper.getMainLooper()).postDelayed({
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                val username = userNameEditText.text.toString()
                val phoneNumber = phoneNumberEditText.text.toString()
                val imageUri = avatarImageView.tag as? Uri

                if (email.isEmpty() || password.isEmpty() || phoneNumber.isEmpty()) {
                    progressBar.visibility = FrameLayout.GONE
                    showAlertDialog("Error", "Please fill in all the required fields.")
                    return@postDelayed
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    progressBar.visibility = FrameLayout.GONE
                    showAlertDialog("Error", "The email address is badly formatted.")
                    Log.d("SignupActivity", "Email: $email")
                    return@postDelayed
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val avatarUrl = if (imageUri != null) {
                        val uploadJob = async {
                            val bitmap = getBitmapFromUri(this@SignupActivity, imageUri)
                            if (bitmap != null) uploadBitmapToFirebaseStorage(bitmap) else null
                        }
                        uploadJob.await()
                    } else {
                        "https://example.com/default-avatar.png" // URL ảnh đại diện mặc định
                    }

                    UserRepository.getInstance()
                        .signUpUser(avatarUrl ?: "https://example.com/default-avatar.png", email, password, username, phoneNumber) { success, uid ->

                        if (success) {
                                val user = UserRepository.getInstance().auth.currentUser
                                user?.sendEmailVerification()?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        showAlertDialogAndNavigate(
                                            "Success",
                                            "Please check your email to confirm your account!",
                                            this@SignupActivity,
                                            LoginActivity::class.java
                                        )
                                    } else {
                                        progressBar.visibility = FrameLayout.GONE
                                        showAlertDialog(
                                            "Error",
                                            "Failed to send verification email."
                                        )
                                    }
                                }
                            } else {
                                progressBar.visibility = FrameLayout.GONE
                                showAlertDialog("Error", "Failed to sign up.")
                            }
                        }
                }
            }, 100)
        }
    }

    private fun getBitmapFromUri(context: Context, imageUri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            BitmapDrawable(context.resources, inputStream).bitmap
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun uploadBitmapToFirebaseStorage(bitmap: Bitmap): String? {
        return suspendCancellableCoroutine { continuation ->
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("avatars/${System.currentTimeMillis()}.png")

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val data = baos.toByteArray()

            val uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    continuation.resume(uri.toString(), null)
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
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

    private fun showAlertDialogAndNavigate(
        title: String,
        message: String,
        context: Context,
        destination: Class<*>
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(context, destination)
            context.startActivity(intent)
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }
}
