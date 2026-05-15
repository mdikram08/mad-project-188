package com.example.smarthostel

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class FoodReviewActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: FoodReviewAdapter
    private var selectedDate = ""
    private var currentUserName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_review)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val btnDate = findViewById<Button>(R.id.btnDate)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val etComment = findViewById<EditText>(R.id.etComment)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitReview)
        val rvReviews = findViewById<RecyclerView>(R.id.rvFoodReviews)

        fetchUserData()

        btnDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = "$day/${month + 1}/$year"
                btnDate.text = selectedDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating.toInt()
            val comment = etComment.text.toString().trim()
            val userId = auth.currentUser?.uid

            if (selectedDate.isEmpty() || rating == 0) {
                Toast.makeText(this, "Please select date and rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId != null) {
                val id = firestore.collection("food_reviews").document().id
                val review = FoodReview(
                    id = id,
                    userId = userId,
                    userName = currentUserName,
                    date = selectedDate,
                    rating = rating,
                    comment = comment
                )
                
                firestore.collection("food_reviews").document(id).set(review)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Review Submitted", Toast.LENGTH_SHORT).show()
                        etComment.text.clear()
                        ratingBar.rating = 0f
                    }
            }
        }

        rvReviews.layoutManager = LinearLayoutManager(this)
        adapter = FoodReviewAdapter()
        rvReviews.adapter = adapter

        fetchReviews()
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener {
                currentUserName = it.getString("name") ?: "Unknown"
            }
        }
    }

    private fun fetchReviews() {
        // For Students, show all reviews? Usually food reviews are public.
        firestore.collection("food_reviews")
            .addSnapshotListener { value, _ ->
                val list = value?.toObjects(FoodReview::class.java) ?: emptyList()
                adapter.submitList(list)
            }
    }
}

class FoodReviewAdapter : RecyclerView.Adapter<FoodReviewAdapter.ViewHolder>() {

    private var items = listOf<FoodReview>()

    fun submitList(newList: List<FoodReview>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvDate.text = "${item.userName} - ${item.date}"
        holder.ratingDisplay.rating = item.rating.toFloat()
        holder.tvComment.text = item.comment
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val ratingDisplay: RatingBar = view.findViewById(R.id.ratingDisplay)
        val tvComment: TextView = view.findViewById(R.id.tvComment)
    }
}
