package com.example.smarthostel

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

class ComplaintActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: ComplaintAdapter
    private var currentUserName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complaint)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val spinner = findViewById<Spinner>(R.id.spinnerCategory)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitComplaint)
        val rvComplaints = findViewById<RecyclerView>(R.id.rvComplaints)

        val categories = arrayOf("Electricity", "Water", "Cleaning", "Others")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        fetchUserData()

        btnSubmit.setOnClickListener {
            val category = spinner.selectedItem.toString()
            val description = etDescription.text.toString().trim()
            val userId = auth.currentUser?.uid

            if (description.isEmpty()) {
                Toast.makeText(this, "Please describe the issue", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId != null) {
                val id = firestore.collection("complaints").document().id
                val complaint = Complaint(
                    id = id,
                    userId = userId,
                    userName = currentUserName,
                    category = category,
                    description = description,
                    status = "pending"
                )
                
                firestore.collection("complaints").document(id).set(complaint)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Complaint Submitted", Toast.LENGTH_SHORT).show()
                        etDescription.text.clear()
                    }
            }
        }

        rvComplaints.layoutManager = LinearLayoutManager(this)
        adapter = ComplaintAdapter(false) { _, _ -> }
        rvComplaints.adapter = adapter

        fetchMyComplaints()
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener {
                currentUserName = it.getString("name") ?: "Unknown"
            }
        }
    }

    private fun fetchMyComplaints() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("complaints")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { value, _ ->
                    val list = value?.toObjects(Complaint::class.java) ?: emptyList()
                    adapter.submitList(list)
                }
        }
    }
}

class ComplaintAdapter(private val isWarden: Boolean, private val onAction: (Complaint, String) -> Unit) :
    RecyclerView.Adapter<ComplaintAdapter.ViewHolder>() {

    private var items = listOf<Complaint>()

    fun submitList(newList: List<Complaint>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_complaint, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvCategory.text = if (isWarden) "${item.userName} - ${item.category}" else item.category
        holder.tvDescription.text = item.description
        holder.tvStatus.text = "Status: ${item.status}"

        if (isWarden && item.status == "pending") {
            holder.layoutWardenActions.visibility = View.VISIBLE
            holder.btnResolve.setOnClickListener { onAction(item, "resolved") }
            holder.btnReject.setOnClickListener { onAction(item, "rejected") }
        } else {
            holder.layoutWardenActions.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val layoutWardenActions: View = view.findViewById(R.id.layoutWardenActions)
        val btnResolve: Button = view.findViewById(R.id.btnResolve)
        val btnReject: Button = view.findViewById(R.id.btnReject)
    }
}
