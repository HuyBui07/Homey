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
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.homey.R
import com.example.homey.data.repository.UserRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resumeWithException

class EditAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_account)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.title = "Edit Account"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val progressBar = findViewById<FrameLayout>(R.id.loadingOverlay)

        val avatarImageView = findViewById<ImageView>(R.id.avatarImage)
        var isImageChanged = false
        val usernameEditText = findViewById<EditText>(R.id.userNameEditText)
        val phoneEditText = findViewById<EditText>(R.id.phoneNumberEditText)

        val imageUrl = UserRepository.getInstance().getImageUrl()
        if (imageUrl != null) {
            Glide.with(this)
                .load(imageUrl)
                .transform(CircleCrop())
                .into(avatarImageView)
            avatarImageView.tag = imageUrl
        }

        val username = UserRepository.getInstance().getUsername()
        if (username != null) {
            usernameEditText.setText(username)
        }

        val phone = UserRepository.getInstance().getPhoneNumber()
        if (phone != null) {
            phoneEditText.setText(phone)
        }

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
                        isImageChanged = true
                    }
                }
            }

        avatarImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            selectImageLauncher.launch(intent)
        }

        val editButton = findViewById<Button>(R.id.editButton)
        editButton.setOnClickListener {
            progressBar.visibility = FrameLayout.VISIBLE

            Handler(Looper.getMainLooper()).postDelayed({
                val newUsername = usernameEditText.text.toString()
                val newPhoneNumber = phoneEditText.text.toString()

                if (newPhoneNumber.isEmpty() || newUsername.isEmpty()) {
                    progressBar.visibility = FrameLayout.GONE
                    showAlertDialog("Error", "Please fill in all the fields.")
                    return@postDelayed
                }

                CoroutineScope(Dispatchers.IO).launch {
                    var newImageUrl: String? = imageUrl
                    if (isImageChanged) {
                        val deleteJob =
                            async {
                                if (imageUrl != null) {
                                    deleteImageFromStorage(
                                        imageUrl,
                                        {
                                            Log.d("EditAccountActivity", "Deleted image from storage.")
                                        },
                                        { exception ->
                                            Log.e(
                                                "EditAccountActivity",
                                                "Failed to delete image from storage.",
                                                exception
                                            )
                                        }
                                    )
                                }
                            }
                        deleteJob.await()

                        val imageUri = avatarImageView.tag as? Uri

                        val bitmap = getBitmapFromUri(this@EditAccountActivity, imageUri!!)
                        if (bitmap == null) {
                            progressBar.visibility = FrameLayout.GONE
                            showAlertDialog("Error", "Failed to load the image.")
                            return@launch
                        }

                        newImageUrl = uploadBitmapToFirebaseStorage(bitmap)
                        if (newImageUrl == null) {
                            progressBar.visibility = FrameLayout.GONE
                            showAlertDialog("Error", "Failed to upload the image.")
                            return@launch
                        }
                    }


                    UserRepository.getInstance().editUserAccount(
                        newImageUrl!!,
                        newUsername,
                        newPhoneNumber
                    ) { success ->
                        progressBar.visibility = FrameLayout.GONE
                        if (success) {
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            showAlertDialog("Error", "Failed to edit the account.")
                        }
                    }
                }
            }, 100)
        }
    }

    private fun deleteImageFromStorage(
        imageUrl: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        storageRef.delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
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
        builder.setPositiveButton("OK", null)
        val alertDialog = builder.create()
        alertDialog.show()
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}