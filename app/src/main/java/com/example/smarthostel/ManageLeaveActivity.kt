package com.example.smarthostel

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ManageLeaveActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: LeaveAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warden_list) // I need to create this simple list layout

        findViewById<TextView>(R.id.tvTitle).text = "Manage Leave Requests"
        firestore = FirebaseFirestore.getInstance()

        val rv = findViewById<RecyclerView>(R.id.rvList)
        rv.layoutManager = LinearLayoutManager(this)
        
        adapter = LeaveAdapter(true) { leave, newStatus ->
            updateStatus(leave.id, newStatus)
        }
        rv.adapter = adapter

        fetchRequests()
    }

    private fun fetchRequests() {
        firestore.collection("leaves").addSnapshotListener { value, _ ->
            val list = value?.toObjects(LeaveRequest::class.java) ?: emptyList()
            adapter.submitList(list)
        }
    }

    private fun updateStatus(id: String, status: String) {
        firestore.collection("leaves").document(id).update("status", status)
            .addOnSuccessListener { Toast.makeText(this, "Updated to $status", Toast.LENGTH_SHORT).show() }
    }
}
