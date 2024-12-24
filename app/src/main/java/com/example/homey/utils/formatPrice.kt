package com.example.homey.utils

fun formatPrice(price: Double): String {
    return if (price >= 1_000_000_000) {
        "${price / 1_000_000_000} tỉ"
    } else {
        "${price / 1_000_000} triệu"
    }
}