package com.example.homey.data.model

data class Post(
    val id: String,
    val imageMain: String,
    val smallImage1:String,
    val smallImage2: String,
    val smallImage3: String,
    val title: String,
    val price: Double,
    val area: Double,
    val location: String,
    val avatar: String,
    val userName: String,
    val phoneNumber: String,
    val bedrooms: Int,
    val bathrooms: Int,
    val postTime: String,
    var isFavorite: Boolean
)
