package com.example.homey.utils

fun formatPrice(price: Double): String {
    return if (price >= 1_000_000_000) {
        String.format("%.1f tỉ", price / 1_000_000_000)
    } else {
        String.format("%.1f triệu", price / 1_000_000)
    }
}