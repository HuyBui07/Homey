package com.example.homey.ui

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
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts

// Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.example.homey.data.repository.EstateRepository

class AddRealEstateActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val estateRepo = EstateRepository.getInstance()

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

        val addPropertyButton = findViewById<Button>(R.id.addPropertyButton)
        addPropertyButton.setOnClickListener {
            val ownerRef = db.collection("users").document("qZ75wqytWzYGmI2M9OUO")

            // Add property to Firestore
            val title = findViewById<EditText>(R.id.titleEditText).text.toString()
            val propertyType = findViewById<Spinner>(R.id.propertyTypeSpinner).selectedItem.toString()
            val location = findViewById<EditText>(R.id.locationEditText).text.toString()
            val price = findViewById<EditText>(R.id.priceEditText).text.toString().toDouble()
            val size = findViewById<EditText>(R.id.sizeEditText).text.toString().toDouble()
            val bedrooms = findViewById<EditText>(R.id.bedroomsEditText).text.toString().toInt()
            val bathrooms = findViewById<EditText>(R.id.bathroomsEditText).text.toString().toInt()
            val estate = Estate(
                title = title,
                propertyType = propertyType,
                location = location,
                price = price,
                size = size,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                ownerRef = ownerRef
            )

            estateRepo.addEstate(estate) { isSuccess ->
                if (isSuccess) {
                    // Property added successfully
                } else {
                    // Property not added
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}