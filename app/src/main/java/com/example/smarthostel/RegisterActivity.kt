package com.example.smarthostel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etYear = findViewById<EditText>(R.id.etYear)
        val etDepartment = findViewById<EditText>(R.id.etDepartment)
        val etRoomNumber = findViewById<EditText>(R.id.etRoomNumber)
        val etPhoneNumber = findViewById<EditText>(R.id.etPhoneNumber)
        
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        tvLogin.setOnClickListener {
            finish() // go back to login
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val year = etYear.text.toString().trim()
            val department = etDepartment.text.toString().trim()
            val roomNumber = etRoomNumber.text.toString().trim()
            val phoneNumber = etPhoneNumber.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Name, Email, and Password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val user = User(
                                userId = userId,
                                name = name,
                                email = email,
                                year = year,
                                department = department,
                                roomNumber = roomNumber,
                                phoneNumber = phoneNumber,
                                role = "Student" // Default role
                            )
                            
                            firestore.collection("users").document(userId).set(user)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, StudentDashboardActivity::class.java))
                                    finishAffinity()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
