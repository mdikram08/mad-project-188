package com.example.smarthostel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashboard)

        auth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.btnLeaveRequest).setOnClickListener {
            startActivity(Intent(this, LeaveRequestActivity::class.java))
        }

        findViewById<Button>(R.id.btnComplaintSystem).setOnClickListener {
            startActivity(Intent(this, ComplaintActivity::class.java))
        }

        findViewById<Button>(R.id.btnFoodReview).setOnClickListener {
            startActivity(Intent(this, FoodReviewActivity::class.java))
        }

        findViewById<Button>(R.id.btnRoomRequest).setOnClickListener {
            startActivity(Intent(this, RoomRequestActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }
}
