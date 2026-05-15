package com.example.smarthostel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class WardenDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warden_dashboard)

        auth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.btnManageLeaveRequests).setOnClickListener {
            startActivity(Intent(this, ManageLeaveActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewComplaints).setOnClickListener {
            startActivity(Intent(this, ManageComplaintActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewRoomRequests).setOnClickListener {
            startActivity(Intent(this, ManageRoomActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewFoodFeedback).setOnClickListener {
            startActivity(Intent(this, ViewFoodReviewsActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }
}
