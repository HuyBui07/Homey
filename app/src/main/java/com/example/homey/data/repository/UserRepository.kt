package com.example.homey.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository private constructor() {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // User information
    private var avatarUrl: String? = null
    private var email: String? = null
    private var username: String? = null
    private var phoneNumber: String? = null

    fun signUpUser(
        avatarUrl: String,
        email: String,
        password: String,
        username: String,
        phoneNumber: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val user = hashMapOf(
                            "uid" to uid,
                            "email" to email,
                            "username" to username,
                            "phoneNumber" to phoneNumber,
                            "avatarUrl" to avatarUrl,
                        )
                        db.collection("users").document(uid).set(user)
                            .addOnSuccessListener {
                                onComplete(true, uid)
                            }
                            .addOnFailureListener {
                                onComplete(false, null)
                            }
                    } else {
                        onComplete(false, null)
                    }
                } else {
                    onComplete(false, null)
                }
            }
    }

    fun loginUser(email: String, password: String, onComplete: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                this.avatarUrl = document["avatarUrl"] as String
                                this.username = document["username"] as String
                                this.email = document["email"] as String
                                this.phoneNumber = document["phoneNumber"] as String
                                onComplete(true)
                            }
                            .addOnFailureListener {
                                onComplete(false)
                            }
                    } else {
                        onComplete(false)
                    }
                } else {
                    onComplete(false)
                }
            }
    }

    fun logoutUser() {
        auth.signOut()
        avatarUrl = null
        username = null
        email = null
        phoneNumber = null
    }

    fun editUserAccount(
        avatarUrl: String,
        username: String,
        phoneNumber: String,
        onComplete: (Boolean) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val user = hashMapOf(
                "avatarUrl" to avatarUrl,
                "username" to username,
                "phoneNumber" to phoneNumber
            )
            db.collection("users").document(uid).update(user as Map<String, Any>)
                .addOnSuccessListener {
                    this.avatarUrl = avatarUrl
                    this.username = username
                    this.phoneNumber = phoneNumber
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        } else {
            onComplete(false)
        }
    }

    fun getImageUrl(): String? {
        return avatarUrl
    }

    fun getUsername(): String? {
        return username
    }

    fun getEmail(): String? {
        return email
    }

    fun getPhoneNumber(): String? {
        return phoneNumber
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: UserRepository().also { instance = it }
        }
    }
}