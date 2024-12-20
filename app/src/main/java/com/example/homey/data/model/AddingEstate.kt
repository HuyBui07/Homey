package com.example.homey.data.model

import com.google.firebase.firestore.DocumentReference

data class AddingEstate(
    val title: String,
    val propertyType: String,
    val location: String,
    val price: Double,
    val size: Double,
    val bedrooms: Int,
    val bathrooms: Int,
    val ownerUid: String,
    val images: MutableList<String>
) {
    constructor() : this("", "", "", 0.0, 0.0, 0, 0, "", mutableListOf())
}
