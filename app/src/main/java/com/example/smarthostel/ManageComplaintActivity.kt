package com.example.smarthostel

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ManageComplaintActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: ComplaintAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warden_list)

        findViewById<TextView>(R.id.tvTitle).text = "Manage Complaints"
        firestore = FirebaseFirestore.getInstance()

        val rv = findViewById<RecyclerView>(R.id.rvList)
        rv.layoutManager = LinearLayoutManager(this)
        
        adapter = ComplaintAdapter(true) { complaint, newStatus ->
            updateStatus(complaint.id, newStatus)
        }
        rv.adapter = adapter

        fetchComplaints()
    }

    private fun fetchComplaints() {
        firestore.collection("complaints").addSnapshotListener { value, _ ->
            val list = value?.toObjects(Complaint::class.java) ?: emptyList()
            adapter.submitList(list)
        }
    }

    private fun updateStatus(id: String, status: String) {
        firestore.collection("complaints").document(id).update("status", status)
            .addOnSuccessListener { Toast.makeText(this, "Updated to $status", Toast.LENGTH_SHORT).show() }
    }
}
