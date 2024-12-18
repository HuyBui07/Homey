package com.example.homey.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homey.R
import com.example.homey.data.model.Estate
import android.content.Intent
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.FrameLayout


// Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.example.homey.data.repository.EstateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resumeWithException

import com.example.homey.data.model.AddingEstate

class AddRealEstateActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    private val estateRepo = EstateRepository.getInstance()

    private lateinit var addImagesButton: Button
    private lateinit var progressBar: FrameLayout
    private val imageUris = mutableListOf<Uri>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_real_estate)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set the title of the action bar
        supportActionBar?.title = "Add Real Estate"

        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        progressBar = findViewById<FrameLayout>(R.id.loadingOverlay)

        // Interactivity
        val locationEditTextView = findViewById<EditText>(R.id.locationEditText)
        locationEditTextView.isFocusable = false
        locationEditTextView.isFocusableInTouchMode = false
        val getLocation =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val data: Intent? = it.data
                    val location = data?.getStringExtra("location")
                    if (location != null) locationEditTextView.setText(location)
                    else locationEditTextView.setText("Location not specified")
                }
            }
        locationEditTextView.setOnClickListener {
            val intent = Intent(this, SpecifyLocationActivity::class.java)
            getLocation.launch(intent)
        }

        addImagesButton = findViewById<Button>(R.id.addImagesButton)
        val imagesTextView = findViewById<TextView>(R.id.imagesCounter)

        val selectImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    progressBar.visibility = FrameLayout.VISIBLE
                    val newImagesUri = mutableListOf<Uri>()
                    val data = result.data
                    if (data != null) {
                        val clipData = data.clipData
                        if (clipData != null) {
                            for (i in 0 until clipData.itemCount) {
                                if (imageUris.size == 4) {
                                    addImagesButton.visibility = Button.GONE
                                    Toast.makeText(
                                        this,
                                        "You can only select up to 4 images",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    break
                                }
                                val imageUri = clipData.getItemAt(i).uri
                                imageUris.add(imageUri)
                                newImagesUri.add(imageUri)
                                imagesTextView.text = "Images (${imageUris.size}/4)"
                                if (imageUris.size == 4) {
                                    addImagesButton.visibility = Button.GONE
                                }
                                // Access the URI of the selected image
                                Toast.makeText(
                                    this,
                                    "Selected Image URI: $imageUri",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            val imageUri = data.data
                            if (imageUri != null) {
                                imageUris.add(imageUri)
                                newImagesUri.add(imageUri)
                                imagesTextView.text = "Images (${imageUris.size}/4)"
                                if (imageUris.size == 4) {
                                    addImagesButton.visibility = Button.GONE
                                }
                                // Access the URI of the selected image
                                Toast.makeText(
                                    this,
                                    "Selected Image URI: $imageUri",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    val imagesLinearLayout = findViewById<LinearLayout>(R.id.imagesLinearLayout)
                    val inflater = LayoutInflater.from(this)

                    for (imageUri in newImagesUri) {
                        val itemView = inflater.inflate(
                            R.layout.added_image_container,
                            imagesLinearLayout,
                            false
                        )

                        val imageUriTextView = itemView.findViewById<TextView>(R.id.image_uri)
                        imageUriTextView.text = imageUri.toString()
                        imageUriTextView.setOnClickListener {
                            showImagePopup(imageUri)
                        }

                        val removeImageButton =
                            itemView.findViewById<ImageButton>(R.id.delete_image)
                        removeImageButton.setOnClickListener {
                            imageUris.remove(imageUri)
                            imagesLinearLayout.removeView(itemView)
                            addImagesButton.visibility = Button.VISIBLE
                            imagesTextView.text = "Images (${imageUris.size}/4)"
                        }

                        imagesLinearLayout.addView(itemView)
                    }

                    newImagesUri.clear()
                    progressBar.visibility = FrameLayout.GONE
                }
            }

        addImagesButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            selectImageLauncher.launch(intent)
        }

        val addPropertyButton = findViewById<Button>(R.id.addPropertyButton)
        addPropertyButton.setOnClickListener {
            Log.d("AddRealEstateActivity", "Add property button clicked")
            // Show the progress bar immediately

            progressBar.visibility = FrameLayout.VISIBLE
            Log.d("AddRealEstateActivity", "Loading overlay visible")


            // Delay the execution of database operations to ensure the progress bar is visible
            Handler(Looper.getMainLooper()).postDelayed({
                val ownerRef = db.collection("users").document("qZ75wqytWzYGmI2M9OUO")

                // Add property to Firestore
                val title = findViewById<EditText>(R.id.titleEditText).text.toString()
                val propertyType =
                    findViewById<Spinner>(R.id.propertyTypeSpinner).selectedItem.toString()
                val location = findViewById<EditText>(R.id.locationEditText).text.toString()
                val priceText = findViewById<EditText>(R.id.priceEditText).text.toString()
                val sizeText = findViewById<EditText>(R.id.sizeEditText).text.toString()
                val bedroomsText = findViewById<EditText>(R.id.bedroomsEditText).text.toString()
                val bathroomsText = findViewById<EditText>(R.id.bathroomsEditText).text.toString()

                if (title.isEmpty() || location.isEmpty() || priceText.isEmpty() || sizeText.isEmpty() || bedroomsText.isEmpty() || bathroomsText.isEmpty()) {
                    progressBar.visibility = FrameLayout.GONE
                    showAlertDialog("Error", "Please fill in all fields")
                    return@postDelayed
                }

                val price = priceText.toDoubleOrNull()
                val size = sizeText.toDoubleOrNull()
                val bedrooms = bedroomsText.toIntOrNull()
                val bathrooms = bathroomsText.toIntOrNull()

                if (price == null || size == null || bedrooms == null || bathrooms == null) {
                    progressBar.visibility = FrameLayout.GONE
                    showAlertDialog("Error", "Please enter valid numbers")
                    return@postDelayed
                }

                if (imageUris.size < 4) {
                    progressBar.visibility = FrameLayout.GONE
                    showAlertDialog("Error", "Please select at least 4 images")
                    return@postDelayed
                }

                val estate = AddingEstate(
                    title,
                    propertyType,
                    location,
                    price,
                    size,
                    bedrooms,
                    bathrooms,
                    ownerRef,
                    mutableListOf()
                )

                CoroutineScope(Dispatchers.IO).launch {
                    val uploadJobs = imageUris.map { imageUri ->
                        async {
                            val bitmap = getBitmapFromUri(this@AddRealEstateActivity, imageUri)
                            if (bitmap != null) {
                                uploadBitmapToFirebaseStorage(bitmap)
                            } else {
                                null
                            }
                        }
                    }

                    val imageUrls = uploadJobs.awaitAll().filterNotNull()
                    estate.images.addAll(imageUrls)

                    withContext(Dispatchers.Main) {
                        if (estate.images.size == imageUris.size) {
                            estateRepo.addEstate(estate) { isSuccess ->
                                progressBar.visibility = FrameLayout.GONE
                                if (isSuccess) {
                                    setResult(RESULT_OK)
                                    finish()
                                } else {
                                    showAlertDialog("Error", "Failed to add property")
                                }
                            }
                        } else {
                            progressBar.visibility = FrameLayout.GONE
                            showAlertDialog("Error", "Failed to upload all images")
                        }
                    }
                }
            }, 100)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun showImagePopup(imageUri: Uri) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_image_popup)
        val popupImageView = dialog.findViewById<ImageView>(R.id.popup_image)
        popupImageView.setImageURI(imageUri)
        dialog.show()
    }


    private fun getBitmapFromUri(context: Context, imageUri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            BitmapDrawable(context.resources, inputStream).bitmap
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun uploadBitmapToFirebaseStorage(bitmap: Bitmap): String? {
        return suspendCancellableCoroutine { continuation ->
            val storageRef = estateRepo.storageRef
            val imageRef = storageRef.child("images/${System.currentTimeMillis()}.png")

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
}