package com.example.homey.data.model

data class Estate(
    var id: String?,
    val title: String,
    val propertyType: String,
    val location: String,
    val latitude: Double, // Thêm latitude
    val longitude: Double, // Thêm longitude
    val price: Double,
    val size: Double,
    val bedrooms: Int,
    val bathrooms: Int,
    val ownerUid: String,
    val images: MutableList<String>,
    val postTime: String,
    val description: String,
    val frontage: Double,
    val orientation: String,
    val legalStatus: String,
    val furnishings: String,
) {
    // Constructor mặc định
    constructor() : this(
        null, "", "", "", 0.0, 0.0, 0.0, 0.0, 0, 0, "", mutableListOf(),
        "", "", 0.0, "", "", ""
    )
}
