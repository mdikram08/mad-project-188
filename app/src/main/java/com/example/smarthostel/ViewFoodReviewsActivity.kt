package com.example.smarthostel

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ViewFoodReviewsActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: FoodReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warden_list)

        findViewById<TextView>(R.id.tvTitle).text = "All Food Reviews"
        firestore = FirebaseFirestore.getInstance()

        val rv = findViewById<RecyclerView>(R.id.rvList)
        rv.layoutManager = LinearLayoutManager(this)
        
        adapter = FoodReviewAdapter()
        rv.adapter = adapter

        fetchReviews()
    }

    private fun fetchReviews() {
        firestore.collection("food_reviews").addSnapshotListener { value, _ ->
            val list = value?.toObjects(FoodReview::class.java) ?: emptyList()
            adapter.submitList(list)
        }
    }
}
