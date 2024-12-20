package com.example.homey.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthManager {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // Lấy người dùng hiện tại
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    // Đăng ký người dùng mới
    fun registerUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception?.message ?: "Registration failed")
                }
            }
    }

    // Đăng nhập người dùng
    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception?.message ?: "Login failed")
                }
            }
    }

    // Đăng xuất người dùng
    fun logout() {
        firebaseAuth.signOut()
    }

    // Kiểm tra xem người dùng đã đăng nhập chưa
    fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}