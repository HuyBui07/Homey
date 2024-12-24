package com.example.homey.data.repository

import android.util.Log
import com.example.homey.data.model.AddingEstate
import com.example.homey.data.model.Estate
import com.example.homey.utils.calculateLatLonRange
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EstateRepository private constructor() {
    private val db = FirebaseFirestore.getInstance()
    val storageRef = FirebaseStorage.getInstance().reference

    fun addEstate(estate: AddingEstate, onComplete: (Boolean) -> Unit) {
        db.collection("estates")
            .add(estate)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun getEstates(latitude: Double, longitude: Double, radius: Double, onComplete: (List<Estate>?) -> Unit) {
        val latLonRang = calculateLatLonRange(latitude, longitude, radius)
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("estates").whereGreaterThanOrEqualTo("lat", latLonRang.first.first)
            .whereLessThanOrEqualTo("lat", latLonRang.first.second)
            .whereGreaterThanOrEqualTo("lon", latLonRang.second.first)
            .whereLessThanOrEqualTo("lon", latLonRang.second.second)
            .whereNotEqualTo("ownerUid", currentUserUid)
            .get()
            .addOnSuccessListener { result ->
                val estates = result.documents.mapNotNull { document ->
                    document.toObject(Estate::class.java)?.apply {
                        id = document.id
                    }
                }
                onComplete(estates)
            }
            .addOnFailureListener { exception ->
                Log.d("EstateRepository", "Failed to get estates: $exception")
                // Handle the error
                onComplete(null)
            }
    }

    fun updateEstate(
        estateId: String,
        updatedFields: Map<String, Any>,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("estates").document(estateId)
            .update(updatedFields)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun deleteEstate(estateId: String, onComplete: (Boolean) -> Unit) {
        // Delete estate from Firestore
        db.collection("estates").document(estateId)
            .delete()
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun getEstateById(estateId: String, onComplete: (Estate?) -> Unit) {
        // Get estate by ID from Firestore
        db.collection("estates").document(estateId)
            .get()
            .addOnSuccessListener { document ->
                val estate = document.toObject(Estate::class.java)
                estate?.id = document.id
                onComplete(estate)
            }
            .addOnFailureListener { exception ->
                // Handle the error
                onComplete(null)
            }
    }

    fun getEstateFurtherInformation(estateId: String, onComplete: (description: String?, frontage: Int?, orientation: String?, legalStatus: String?, furnishings: String?) -> Unit) {
        // Get estate by ID from Firestore
        db.collection("estates").document(estateId)
            .get()
            .addOnSuccessListener { document ->
                val description = document.getString("description")
                val frontage = document.getLong("frontage")?.toInt()
                val orientation = document.getString("orientation")
                val legalStatus = document.getString("legalStatus")
                val furnishings = document.getString("furnishings")
                onComplete(description, frontage, orientation, legalStatus, furnishings)
            }
            .addOnFailureListener { exception ->
            }
    }

    fun getEstatesByOwner(userUid: String, onComplete: (List<Estate>?) -> Unit) {
        // Get estates by owner from Firestore
        db.collection("estates")
            .whereEqualTo("ownerUid", userUid)
            .get()
            .addOnSuccessListener { result ->
                val estates = result.documents.mapNotNull { document ->
                    document.toObject(Estate::class.java)?.apply {
                        id = document.id
                    }
                }
                onComplete(estates)
            }
            .addOnFailureListener { exception ->
                // Handle the error
                onComplete(null)
            }
    }

    companion object {
        @Volatile
        private var instance: EstateRepository? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: EstateRepository().also { instance = it }
        }
    }
}