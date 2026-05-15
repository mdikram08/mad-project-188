package com.example.smarthostel

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class LeaveRequestActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: LeaveAdapter
    private var fromDateStr = ""
    private var toDateStr = ""
    private var currentUserName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leave_request)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val btnFromDate = findViewById<Button>(R.id.btnFromDate)
        val btnToDate = findViewById<Button>(R.id.btnToDate)
        val etReason = findViewById<EditText>(R.id.etReason)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitLeave)
        val rvLeaveRequests = findViewById<RecyclerView>(R.id.rvLeaveRequests)

        fetchUserData()

        btnFromDate.setOnClickListener {
            showDatePicker { date ->
                fromDateStr = date
                btnFromDate.text = date
            }
        }

        btnToDate.setOnClickListener {
            showDatePicker { date ->
                toDateStr = date
                btnToDate.text = date
            }
        }

        btnSubmit.setOnClickListener {
            val reason = etReason.text.toString().trim()
            val userId = auth.currentUser?.uid

            // 5. Validation Before Submit
            if (fromDateStr.isEmpty() || toDateStr.isEmpty() || reason.isEmpty()) {
                Toast.makeText(this, "Please fill all fields: From Date, To Date, and Reason", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId != null) {
                val id = firestore.collection("leaves").document().id
                
                // 3. Firestore Data Consistency (Using named arguments to avoid positional mismatch)
                val leave = LeaveRequest(
                    id = id,
                    userId = userId,
                    userName = currentUserName,
                    fromDate = fromDateStr,
                    toDate = toDateStr,
                    reason = reason,
                    status = "pending"
                )
                
                // 6. Debug Logging
                Log.d("LEAVE_DEBUG", "Submitting leave: from=$fromDateStr to=$toDateStr reason=$reason status=pending")

                firestore.collection("leaves").document(id).set(leave)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Request Submitted Successfully", Toast.LENGTH_SHORT).show()
                        etReason.text.clear()
                        btnFromDate.text = getString(R.string.from_date)
                        btnToDate.text = getString(R.string.to_date)
                        fromDateStr = ""
                        toDateStr = ""
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Submission Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        rvLeaveRequests.layoutManager = LinearLayoutManager(this)
        adapter = LeaveAdapter(false) { _, _ -> }
        rvLeaveRequests.adapter = adapter

        fetchMyRequests()
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener {
                currentUserName = it.getString("name") ?: "Unknown User"
            }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val date = "$day/${month + 1}/$year"
            onDateSelected(date)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun fetchMyRequests() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("leaves")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e("LEAVE_DEBUG", "Error fetching requests", error)
                        return@addSnapshotListener
                    }
                    val list = value?.toObjects(LeaveRequest::class.java) ?: emptyList()
                    adapter.submitList(list)
                }
        }
    }
}

class LeaveAdapter(private val isWarden: Boolean, private val onAction: (LeaveRequest, String) -> Unit) :
    RecyclerView.Adapter<LeaveAdapter.ViewHolder>() {

    private var items = listOf<LeaveRequest>()

    fun submitList(newList: List<LeaveRequest>) {
        items = newList.sortedByDescending { it.id } // Simple sort
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leave_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        // 1 & 4. Correct mapping and ViewHolder binding
        holder.tvFromDate.text = "From: ${item.fromDate}"
        holder.tvToDate.text = "To: ${item.toDate}"
        holder.tvReason.text = "Reason: ${item.reason}"
        
        // 2. Status Display Bug Fix: Only use the status field dynamically
        holder.tvStatus.text = "Status: ${item.status}"

        if (isWarden) {
            holder.tvUserName.visibility = View.VISIBLE
            holder.tvUserName.text = "Student: ${item.userName}"
            if (item.status == "pending") {
                holder.layoutWardenActions.visibility = View.VISIBLE
                holder.btnApprove.setOnClickListener { onAction(item, "approved") }
                holder.btnReject.setOnClickListener { onAction(item, "rejected") }
            } else {
                holder.layoutWardenActions.visibility = View.GONE
            }
        } else {
            holder.tvUserName.visibility = View.GONE
            holder.layoutWardenActions.visibility = View.GONE
        }

        // 6. Debug Logging
        Log.d("LEAVE_DEBUG", "Displaying item: from=${item.fromDate} to=${item.toDate} reason=${item.reason} status=${item.status}")
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvFromDate: TextView = view.findViewById(R.id.tvFromDate)
        val tvToDate: TextView = view.findViewById(R.id.tvToDate)
        val tvReason: TextView = view.findViewById(R.id.tvReason)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val layoutWardenActions: View = view.findViewById(R.id.layoutWardenActions)
        val btnApprove: Button = view.findViewById(R.id.btnApprove)
        val btnReject: Button = view.findViewById(R.id.btnReject)
    }
}
