package com.example.homey.ui

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homey.R
import com.example.homey.data.model.Estate

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

        val addPropertyButton = findViewById<Button>(R.id.addPropertyButton)
        addPropertyButton.setOnClickListener {
            val ownerRef = db.collection("users").document("qZ75wqytWzYGmI2M9OUO")

            // Add property to Firestore
            val propEstate = Estate(
                title = "Villa",
                propertyType = "House",
                location = "Kigali",
                price = 100000.0,
                size = 200.0,
                bedrooms = 4,
                bathrooms = 3,
                ownerRef = ownerRef
            )

            estateRepo.addEstate(propEstate) { isSuccess ->
                if (isSuccess) {
                    // Property added successfully
                } else {
                    // Property not added
                }
            }
        }

    }
}