package com.example.homey.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homey.R
import com.example.homey.data.model.AddingEstate
import com.example.homey.data.repository.EstateRepository
import com.example.homey.data.repository.UserRepository
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resumeWithException

class AddRealEstateActivity : AppCompatActivity() {
    private val estateRepo = EstateRepository.getInstance()
    private lateinit var progressBar: FrameLayout
    private lateinit var locationEditTextView: EditText
    private lateinit var addImagesButton: Button
    private val imageUris = mutableListOf<Uri>()

    // Để lưu tọa độ
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

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

        progressBar = findViewById(R.id.loadingOverlay)
        locationEditTextView = findViewById(R.id.locationEditText)
        locationEditTextView.isFocusable = false
        locationEditTextView.isFocusableInTouchMode = false

        // ActivityResultLauncher để chọn vị trí
        val getLocation =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    locationEditTextView.setText(data?.getStringExtra("location") ?: "Location not specified")
                    latitude = data?.getDoubleExtra("lat", 0.0) ?: 0.0
                    longitude = data?.getDoubleExtra("lon", 0.0) ?: 0.0
                }
            }

        locationEditTextView.setOnClickListener {
            val intent = Intent(this, SpecifyLocationActivity::class.java)
            getLocation.launch(intent)
        }

        // Xử lý thêm ảnh
        addImagesButton = findViewById(R.id.addImagesButton)
        val imagesTextView = findViewById<TextView>(R.id.imagesCounter)

        val selectImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    progressBar.visibility = FrameLayout.VISIBLE
                    val newImagesUri = mutableListOf<Uri>()

                    // Xử lý chọn nhiều ảnh
                    data?.clipData?.let { clipData ->
                        for (i in 0 until clipData.itemCount) {
                            if (imageUris.size == 4) break
                            val imageUri = clipData.getItemAt(i).uri
                            imageUris.add(imageUri)
                            newImagesUri.add(imageUri)
                        }
                    }

                    // Xử lý chọn một ảnh
                    data?.data?.let { uri ->
                        if (imageUris.size < 4) {
                            imageUris.add(uri)
                            newImagesUri.add(uri)
                        }
                    }

                    imagesTextView.text = "Images (${imageUris.size}/4)"
                    if (imageUris.size == 4) addImagesButton.visibility = Button.GONE

                    val imagesLinearLayout = findViewById<LinearLayout>(R.id.imagesLinearLayout)
                    val inflater = layoutInflater

                    for (imageUri in newImagesUri) {
                        val itemView = inflater.inflate(R.layout.added_image_container, imagesLinearLayout, false)
                        val imageUriTextView = itemView.findViewById<TextView>(R.id.image_uri)
                        imageUriTextView.text = imageUri.toString()
                        imageUriTextView.setOnClickListener { showImagePopup(imageUri) }

                        val removeImageButton = itemView.findViewById<ImageButton>(R.id.delete_image)
                        removeImageButton.setOnClickListener {
                            imageUris.remove(imageUri)
                            imagesLinearLayout.removeView(itemView)
                            imagesTextView.text = "Images (${imageUris.size}/4)"
                            addImagesButton.visibility = Button.VISIBLE
                        }

                        imagesLinearLayout.addView(itemView)
                    }

                    progressBar.visibility = FrameLayout.GONE
                }
            }

        addImagesButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            selectImageLauncher.launch(intent)
        }

        // Thêm bất động sản
        findViewById<Button>(R.id.addPropertyButton).setOnClickListener {
            progressBar.visibility = FrameLayout.VISIBLE

            val title = findViewById<EditText>(R.id.titleEditText).text.toString()
            val propertyType = findViewById<Spinner>(R.id.propertyTypeSpinner).selectedItem.toString()
            val location = locationEditTextView.text.toString()
            val price = findViewById<EditText>(R.id.priceEditText).text.toString().toDoubleOrNull()
            val size = findViewById<EditText>(R.id.sizeEditText).text.toString().toDoubleOrNull()
            val bedrooms = findViewById<EditText>(R.id.bedroomsEditText).text.toString().toIntOrNull()
            val bathrooms = findViewById<EditText>(R.id.bathroomsEditText).text.toString().toIntOrNull()
            val description = findViewById<EditText>(R.id.descriptionEditText).text.toString()
            val frontage = findViewById<EditText>(R.id.frontageEditText).text.toString().toIntOrNull()
            val orientation = findViewById<Spinner>(R.id.orientationSpinner).selectedItem.toString()
            val legalStatus = findViewById<EditText>(R.id.legalStatusEditText).text.toString()
            val furnishings = findViewById<EditText>(R.id.furnishingsEditText).text.toString()

            if (title.isEmpty() || location.isEmpty() || price == null || size == null || bedrooms == null ||
                bathrooms == null || description.isEmpty() || frontage == null || legalStatus.isEmpty() ||
                furnishings.isEmpty() || latitude == 0.0 || longitude == 0.0) {
                progressBar.visibility = FrameLayout.GONE
                showAlertDialog("Error", "Please fill in all fields and specify a location")
                return@setOnClickListener
            }

            val ownerUid = UserRepository.getInstance().auth.currentUser?.uid
            val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            val estate = AddingEstate(
                title = title,
                propertyType = propertyType,
                location = location,
                lat = latitude,
                lon = longitude,
                price = price,
                size = size,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                ownerUid = ownerUid ?: "",
                images = mutableListOf(),
                postTime = currentDate,
                description = description,
                frontage = frontage,
                orientation = orientation,
                legalStatus = legalStatus,
                furnishings = furnishings
            )

            CoroutineScope(Dispatchers.IO).launch {
                val uploadJobs = imageUris.map { uri ->
                    async { getBitmapFromUri(this@AddRealEstateActivity, uri)?.let { uploadBitmapToFirebaseStorage(it) } }
                }
                val imageUrls = uploadJobs.awaitAll().filterNotNull()
                estate.images.addAll(imageUrls)

                withContext(Dispatchers.Main) {
                    if (estate.images.size == imageUris.size) {
                        estateRepo.addEstate(estate) { success ->
                            progressBar.visibility = FrameLayout.GONE
                            if (success) {
                                Toast.makeText(this@AddRealEstateActivity, "Property added successfully", Toast.LENGTH_SHORT).show()
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
        }
    }

    private fun showImagePopup(imageUri: Uri) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_image_popup)
        dialog.findViewById<ImageView>(R.id.popup_image).setImageURI(imageUri)
        dialog.show()
    }

    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapDrawable(context.resources, it).bitmap
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun uploadBitmapToFirebaseStorage(bitmap: Bitmap): String? {
        return suspendCancellableCoroutine { continuation ->
            val storageRef = estateRepo.storageRef.child("images/${System.currentTimeMillis()}.png")
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val data = baos.toByteArray()

            storageRef.putBytes(data)
                .addOnSuccessListener { storageRef.downloadUrl.addOnSuccessListener { uri -> continuation.resume(uri.toString(), null) } }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}
