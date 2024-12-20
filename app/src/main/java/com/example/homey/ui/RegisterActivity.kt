package com.example.homey.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.homey.R
import com.example.homey.auth.AuthManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var birthdayEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var interestAreaEditText: EditText
    private lateinit var accountTypeSpinner: Spinner
    private lateinit var registerButton: Button

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        genderSpinner = findViewById(R.id.genderSpinner)
        birthdayEditText = findViewById(R.id.birthdayEditText)
        addressEditText = findViewById(R.id.addressEditText)
        interestAreaEditText = findViewById(R.id.interestAreaEditText)
        accountTypeSpinner = findViewById(R.id.accountTypeSpinner)
        registerButton = findViewById(R.id.registerButton)

        authManager = AuthManager()

        registerButton.setOnClickListener { registerUser() }
    }

    private fun registerUser() {
        val fullName = fullNameEditText.text.toString()
        val email = emailEditText.text.toString()
        val phone = phoneEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()
        val gender = genderSpinner.selectedItem.toString()
        val birthday = birthdayEditText.text.toString()
        val address = addressEditText.text.toString()
        val interestArea = interestAreaEditText.text.toString()
        val accountType = accountTypeSpinner.selectedItem.toString()

        // Validate inputs
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show()
            return
        }

        // Register user in Firebase Authentication
        authManager.registerUser(email, password, {
            // Save user info to Firestore
            saveUserToDatabase(fullName, email, phone, gender, birthday, address, interestArea, accountType)
            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, { error ->
            Toast.makeText(this, "Đăng ký thất bại: $error", Toast.LENGTH_SHORT).show()
        })
    }

    private fun saveUserToDatabase(
        fullName: String,
        email: String,
        phone: String,
        gender: String,
        birthday: String,
        address: String,
        interestArea: String,
        accountType: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val user = hashMapOf(
            "userId" to userId,
            "fullName" to fullName,
            "email" to email,
            "phone" to phone,
            "gender" to gender,
            "birthday" to birthday,
            "address" to address,
            "interestArea" to interestArea,
            "accountType" to accountType
        )

        db.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Thông tin người dùng đã được lưu!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Lưu thất bại: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
