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
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.homey.R
import com.example.homey.data.model.AddingEstate
import com.example.homey.data.model.Estate
import com.example.homey.data.repository.EstateRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resumeWithException

class EditEstateActivity : AppCompatActivity() {
    private val estateRepo = EstateRepository.getInstance()

    // UI elements
    private lateinit var titleEditText: EditText
    private lateinit var propertyTypeSpinner: Spinner
    private lateinit var locationEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var sizeEditText: EditText
    private lateinit var bedroomsEditText: EditText
    private lateinit var bathroomsEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var frontageEditText: EditText
    private lateinit var orientationSpinner: Spinner
    private lateinit var legalStatusEditText: EditText
    private lateinit var furnishingsEditText: EditText
    private lateinit var addImagesButton: Button
    private lateinit var progressBar: FrameLayout

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_estate)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.title = "Edit Real Estate"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        progressBar = findViewById<FrameLayout>(R.id.loadingOverlay)

        titleEditText = findViewById<EditText>(R.id.titleEditText)
        propertyTypeSpinner = findViewById<Spinner>(R.id.propertyTypeSpinner)
        locationEditText = findViewById<EditText>(R.id.locationEditText)
        priceEditText = findViewById<EditText>(R.id.priceEditText)
        sizeEditText = findViewById<EditText>(R.id.sizeEditText)
        bedroomsEditText = findViewById<EditText>(R.id.bedroomsEditText)
        bathroomsEditText = findViewById<EditText>(R.id.bathroomsEditText)
        descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        frontageEditText = findViewById<EditText>(R.id.frontageEditText)
        orientationSpinner = findViewById<Spinner>(R.id.orientationSpinner)
        legalStatusEditText = findViewById<EditText>(R.id.legalStatusEditText)
        furnishingsEditText = findViewById<EditText>(R.id.furnishingsEditText)


        val estateId = intent.getStringExtra("estateId")
        val estateTitle = intent.getStringExtra("estateTitle")
        val estatePropertyType = intent.getStringExtra("estatePropertyType")
        val estatePrice = intent.getDoubleExtra("estatePrice", 0.0)
        val estateSize = intent.getDoubleExtra("estateSize", 0.0)
        val estateLocation = intent.getStringExtra("estateLocation")
        val estateBedrooms = intent.getIntExtra("estateBedrooms", 0)
        val estateBathrooms = intent.getIntExtra("estateBathrooms", 0)
        val estateImages = intent.getStringArrayListExtra("estateImages")

        titleEditText.setText(estateTitle)
        propertyTypeSpinner.setSelection(
            resources.getStringArray(R.array.property_types).indexOf(
                estatePropertyType
            )
        )
        priceEditText.setText(String.format("%.0f", estatePrice))
        sizeEditText.setText(estateSize.toString())
        locationEditText.setText(estateLocation)
        bedroomsEditText.setText(estateBedrooms.toString())
        bathroomsEditText.setText(estateBathrooms.toString())

        estateRepo.getEstateFurtherInformation(estateId!!) { description, frontage, orientation, legalStatus, furnishings ->
            if (description != null && frontage != null && orientation != null && legalStatus != null && furnishings != null) {
                descriptionEditText.setText(description)
                frontageEditText.setText(frontage.toString())
                orientationSpinner.setSelection(
                    resources.getStringArray(R.array.orientations).indexOf(orientation)
                )
                legalStatusEditText.setText(legalStatus)
                furnishingsEditText.setText(furnishings)
            }
        }

        val newlyAddedImages = mutableListOf<Uri>()
        val deletedImagesUrl = mutableListOf<String>()
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
                                if (newlyAddedImages.size + (estateImages!!.size) == 4) {
                                    addImagesButton.visibility = Button.GONE
                                    Toast.makeText(
                                        this,
                                        "You can only add up to 4 images",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    break
                                }
                                val imageUri = clipData.getItemAt(i).uri
                                newlyAddedImages.add(imageUri)
                                newImagesUri.add(imageUri)
                                imagesTextView.text =
                                    "Images (${newlyAddedImages.size + (estateImages!!.size)}/4)"
                                if (newlyAddedImages.size + (estateImages.size) == 4) {
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
                                newlyAddedImages.add(imageUri)
                                newImagesUri.add(imageUri)
                                imagesTextView.text =
                                    "Images (${newlyAddedImages.size + (estateImages!!.size)}/4)"
                                if (newlyAddedImages.size == 4) {
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
                            newlyAddedImages.remove(imageUri)
                            imagesLinearLayout.removeView(itemView)
                            imagesTextView.text =
                                "Images (${newlyAddedImages.size + (estateImages!!.size)}/4)"
                            addImagesButton.visibility = Button.VISIBLE
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

        addImagesButton.visibility = Button.GONE
        imagesTextView.text = "Images (4/4)"

        val imagesLinearLayout = findViewById<LinearLayout>(R.id.imagesLinearLayout)
        val inflater = LayoutInflater.from(this)
        estateImages?.forEach { imageUrl ->
            val itemView = inflater.inflate(
                R.layout.added_image_container,
                imagesLinearLayout,
                false
            )

            val imageUriTextView = itemView.findViewById<TextView>(R.id.image_uri)
            imageUriTextView.text = imageUrl.toString()
            imageUriTextView.setOnClickListener {
                showImagePopupUsingUrl(imageUrl)
            }

            val removeImageButton =
                itemView.findViewById<ImageButton>(R.id.delete_image)
            removeImageButton.setOnClickListener {
                estateImages.remove(imageUrl)
                imagesLinearLayout.removeView(itemView)
                imagesTextView.text = "Images (${imagesLinearLayout.childCount}/4)"
                addImagesButton.visibility = Button.VISIBLE
                deletedImagesUrl.add(imageUrl)
            }

            imagesLinearLayout.addView(itemView)
        }

        locationEditText.isFocusable = false
        locationEditText.isFocusableInTouchMode = false
        val getLocation =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val data: Intent? = it.data
                    val location = data?.getStringExtra("location")
                    if (location != null) locationEditText.setText(location)
                    else locationEditText.setText("Location not specified")
                }
            }
        locationEditText.setOnClickListener {
            val intent = Intent(this, SpecifyLocationActivity::class.java)
            getLocation.launch(intent)
        }

        val editButton = findViewById<Button>(R.id.editPropertyButton)
        editButton.setOnClickListener {
            progressBar.visibility = FrameLayout.VISIBLE

            Handler(Looper.getMainLooper()).postDelayed({
                val title = titleEditText.text.toString()
                val propertyType = propertyTypeSpinner.selectedItem.toString()
                val location = locationEditText.text.toString()
                val price = priceEditText.text.toString().toDouble()
                val size = sizeEditText.text.toString().toDouble()
                val bedrooms = bedroomsEditText.text.toString().toInt()
                val bathrooms = bathroomsEditText.text.toString().toInt()
                val description = descriptionEditText.text.toString()
                val frontage = frontageEditText.text.toString().toInt()
                val orientation = orientationSpinner.selectedItem.toString()
                val legalStatus = legalStatusEditText.text.toString()
                val furnishings = furnishingsEditText.text.toString()

                if (title.isEmpty() || location.isEmpty()) {
                    showAlertDialog("Error", "Title and Location are required")
                    return@postDelayed
                }

                if (price <= 0 || size <= 0 || bedrooms <= 0 || bathrooms <= 0) {
                    showAlertDialog(
                        "Error",
                        "Price, Size, Bedrooms, and Bathrooms must be greater than 0"
                    )
                    return@postDelayed
                }

                if (description.isEmpty() || frontage <= 0 || legalStatus.isEmpty() || furnishings.isEmpty()) {
                    showAlertDialog(
                        "Error",
                        "Description, Frontage, Legal Status, and Furnishings are required"
                    )
                    return@postDelayed
                }

                if (newlyAddedImages.size + estateImages!!.size > 4) {
                    showAlertDialog("Error", "You can only add up to 4 images")
                    return@postDelayed
                }

                deleteImage(estateId, deletedImagesUrl)

                CoroutineScope(Dispatchers.IO).launch {
                    val uploadedImages = newlyAddedImages.map { imageUri ->
                        async {
                            val bitmap = getBitmapFromUri(this@EditEstateActivity, imageUri)
                            if (bitmap != null) {
                                uploadBitmapToFirebaseStorage(bitmap)
                            } else {
                                null
                            }
                        }
                    }

                    val newImages = uploadedImages.awaitAll().filterNotNull().toMutableList()
                    newImages.addAll(estateImages)

                    withContext(Dispatchers.Main) {
                        val updatedFields = mapOf(
                            "title" to title,
                            "propertyType" to propertyType,
                            "location" to location,
                            "price" to price,
                            "size" to size,
                            "bedrooms" to bedrooms,
                            "bathrooms" to bathrooms,
                            "description" to description,
                            "frontage" to frontage,
                            "orientation" to orientation,
                            "legalStatus" to legalStatus,
                            "furnishings" to furnishings,
                            "images" to newImages
                        )

                        estateRepo.updateEstate(estateId, updatedFields) { success ->
                            if (success) {
                                progressBar.visibility = FrameLayout.GONE
                                setResult(RESULT_OK)
                                finish()
                            } else {
                                progressBar.visibility = FrameLayout.GONE
                                Toast.makeText(
                                    this@EditEstateActivity,
                                    "Failed to update estate",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }, 100)
        }

        val deleteButton = findViewById<Button>(R.id.deletePropertyButton)
        deleteButton.setOnClickListener {
            progressBar.visibility = FrameLayout.VISIBLE
            estateRepo.deleteEstate(estateId!!) { success ->
                if (success) {
                    progressBar.visibility = FrameLayout.GONE
                    setResult(RESULT_OK)
                    finish()
                } else {
                    progressBar.visibility = FrameLayout.GONE
                    Toast.makeText(
                        this@EditEstateActivity,
                        "Failed to delete estate",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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

    private fun showImagePopupUsingUrl(imageUrl: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_image_popup)
        val popupImageView = dialog.findViewById<ImageView>(R.id.popup_image)
        Glide.with(this)
            .load(imageUrl)
            .into(popupImageView)
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

    private fun deleteImageReferenceFromFirestore(
        documentId: String,
        imageUrl: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val documentRef = db.collection("estates").document(documentId)

        documentRef.update("images", FieldValue.arrayRemove(imageUrl))
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    private fun deleteImage(documentId: String, imageUrls: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Delete the image from Firebase Storage
                imageUrls.forEach { imageUrl ->
                    deleteImageFromStorage(imageUrl, {
                        Log.d("EditEstateActivity", "Image deleted successfully")
                    }, { exception ->
                        Log.e("EditEstateActivity", "Failed to delete image: ${exception.message}")
                    })
                }

                // Delete the image reference from Firestore
                imageUrls.forEach { imageUrl ->
                    deleteImageReferenceFromFirestore(documentId, imageUrl, {
                        Log.d("EditEstateActivity", "Image reference deleted successfully")
                    }, { exception ->
                        Log.e(
                            "EditEstateActivity",
                            "Failed to delete image reference: ${exception.message}"
                        )
                    })
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@EditEstateActivity,
                        "Image deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@EditEstateActivity,
                        "Failed to delete image: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}