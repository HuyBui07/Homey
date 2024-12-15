package com.example.homey.data.model

import com.google.firebase.firestore.DocumentReference

data class Estate(
    val title: String,
    val propertyType: String,
    val location: String,
    val price: Double,
    val size: Double,
    val bedrooms: Int,
    val bathrooms: Int,
    val ownerRef: DocumentReference,
    val images: MutableList<String>
)
