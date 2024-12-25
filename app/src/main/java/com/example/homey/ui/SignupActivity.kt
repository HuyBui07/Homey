package com.example.homey.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.homey.R
import com.example.homey.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resumeWithException

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

        supportActionBar?.hide()

        val avatarImageView = findViewById<ImageView>(R.id.avatarImage)
        val avatarErrorText = findViewById<TextView>(R.id.avatarErrorText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val userNameEditText = findViewById<EditText>(R.id.userNameEditText)
        val phoneNumberEditText = findViewById<EditText>(R.id.phoneNumberEditText)
        val signUpButton = findViewById<TextView>(R.id.signUpButton)
        val progressBar = findViewById<FrameLayout>(R.id.loadingOverlay)

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
                        avatarErrorText.visibility = View.GONE
                    }
                }
            }

        avatarImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            selectImageLauncher.launch(intent)
        }

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val username = userNameEditText.text.toString().trim()
            val phoneNumber = phoneNumberEditText.text.toString().trim()
            val imageUri = avatarImageView.tag as? Uri

            resetErrorMessages(avatarErrorText, emailEditText, passwordEditText, userNameEditText, phoneNumberEditText)

            var hasError = false

            if (imageUri == null) {
                avatarErrorText.text = "Vui lòng chọn ảnh đại diện"
                avatarErrorText.visibility = View.VISIBLE
                hasError = true
            }
            if (email.isEmpty()) {
                emailEditText.error = "Vui lòng nhập email"
                hasError = true
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Địa chỉ email không hợp lệ"
                hasError = true
            }
            if (password.isEmpty()) {
                passwordEditText.error = "Vui lòng nhập mật khẩu"
                hasError = true
            }
            if (username.isEmpty()) {
                userNameEditText.error = "Vui lòng nhập tên người dùng"
                hasError = true
            }
            if (phoneNumber.isEmpty()) {
                phoneNumberEditText.error = "Vui lòng nhập số điện thoại"
                hasError = true
            }

            if (hasError) return@setOnClickListener

            progressBar.visibility = View.VISIBLE

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uploadJob = CoroutineScope(Dispatchers.IO).async {
                            val bitmap = getBitmapFromUri(this@SignupActivity, imageUri!!)
                            if (bitmap != null) uploadBitmapToFirebaseStorage(bitmap) else null
                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            val avatarUrl = uploadJob.await()
                            if (avatarUrl != null) {
                                UserRepository.getInstance().signUpUser(
                                    avatarUrl, email, password, username, phoneNumber
                                ) { success, _ ->
                                    progressBar.visibility = View.GONE
                                    if (success) {
                                        showAlertDialogAndNavigate(
                                            "Thành công",
                                            "Vui lòng kiểm tra email để xác minh tài khoản!",
                                            this@SignupActivity,
                                            LoginActivity::class.java
                                        )
                                    } else {
                                        showAlertDialog("Lỗi", "Đăng ký không thành công")
                                    }
                                }
                            } else {
                                progressBar.visibility = View.GONE
                                showAlertDialog("Lỗi", "Không thể tải ảnh đại diện")
                            }
                        }
                    } else {
                        progressBar.visibility = View.GONE
                        if (task.exception?.message?.contains("email address is already in use") == true) {
                            emailEditText.error = "Email này đã được sử dụng"
                        } else {
                            showAlertDialog("Lỗi", task.exception?.localizedMessage ?: "Đăng ký thất bại")
                        }
                    }
                }
        }
    }

    private fun resetErrorMessages(
        avatarErrorText: TextView,
        emailEditText: EditText,
        passwordEditText: EditText,
        userNameEditText: EditText,
        phoneNumberEditText: EditText
    ) {
        avatarErrorText.visibility = View.GONE
        emailEditText.error = null
        passwordEditText.error = null
        userNameEditText.error = null
        phoneNumberEditText.error = null
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
