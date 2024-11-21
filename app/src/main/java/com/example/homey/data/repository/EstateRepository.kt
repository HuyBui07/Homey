package com.example.homey.data.repository

import com.example.homey.data.model.Estate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class EstateRepository private constructor() {
    private val db = FirebaseFirestore.getInstance()

    fun addEstate(estate: Estate, onComplete: (Boolean) -> Unit) {
        // Add estate to Firestore
        db.collection("estates")
            .add(estate)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun getEstates(onComplete: (QuerySnapshot?) -> Unit) {
        db.collection("estates")
            .get()
            .addOnSuccessListener { result ->
                onComplete(result)
            }
            .addOnFailureListener { exception ->
                // Handle the error
                onComplete(null)
            }
    }

    fun updateEstate() {
        // Update estate in Firestore
    }

    fun deleteEstate() {
        // Delete estate from Firestore
    }

    fun getEstateById() {
        // Get estate by ID from Firestore
    }

    fun getEstatesByOwner() {
        // Get estates by owner from Firestore
    }

    companion object {
        @Volatile private var instance: EstateRepository? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: EstateRepository().also { instance = it }
        }
    }
}