package com.example.homey.data.model

data class Post(
    val imageMain: Int,
    val smallImage1: Int,
    val smallImage2: Int,
    val smallImage3: Int,
    val title: String,
    val price: String,
    val area: String,
    val pricePerArea: String,
    val location: String,
    val userName: String,
    val postTime: String,
    val phoneNumber: String,
    var isFavorite: Boolean
)
