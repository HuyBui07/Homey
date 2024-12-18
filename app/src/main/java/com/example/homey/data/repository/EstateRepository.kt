package com.example.homey.data.repository

import com.example.homey.data.model.AddingEstate
import com.example.homey.data.model.Estate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class EstateRepository private constructor() {
    private val db = FirebaseFirestore.getInstance()
    val storageRef = FirebaseStorage.getInstance().reference

    fun addEstate(estate: AddingEstate, onComplete: (Boolean) -> Unit) {
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

    fun updateEstate(estateId: String, updatedFields: Map<String, Any>, onComplete: (Boolean) -> Unit) {
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

    fun getEstateById() {
        // Get estate by ID from Firestore
    }

    fun getEstatesByOwner(userUid: String, onComplete: (List<Estate>?) -> Unit) {
        // Get estates by owner from Firestore
        val userRef = db.collection("users").document(userUid)
        db.collection("estates")
            .whereEqualTo("ownerRef", userRef)
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
        @Volatile private var instance: EstateRepository? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: EstateRepository().also { instance = it }
        }
    }
}