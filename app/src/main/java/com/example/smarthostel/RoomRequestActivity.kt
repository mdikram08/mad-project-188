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

class RoomRequestActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: RoomRequestAdapter
    private var currentUserName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_request)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val rgRoomType = findViewById<RadioGroup>(R.id.rgRoomType)
        val spinnerSharing = findViewById<Spinner>(R.id.spinnerSharing)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitRoomRequest)
        val rvRequests = findViewById<RecyclerView>(R.id.rvRoomRequests)

        val sharingOptions = arrayOf("2 Sharing", "3 Sharing", "5 Sharing")
        spinnerSharing.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sharingOptions)

        fetchUserData()

        btnSubmit.setOnClickListener {
            val selectedTypeId = rgRoomType.checkedRadioButtonId
            val roomType = findViewById<RadioButton>(selectedTypeId).text.toString()
            val sharing = spinnerSharing.selectedItem.toString()
            val userId = auth.currentUser?.uid

            if (userId != null) {
                val id = firestore.collection("room_requests").document().id
                val request = RoomRequest(
                    id = id,
                    userId = userId,
                    userName = currentUserName,
                    acType = roomType,
                    sharing = sharing,
                    status = "pending"
                )
                
                firestore.collection("room_requests").document(id).set(request)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Request Submitted", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        rvRequests.layoutManager = LinearLayoutManager(this)
        adapter = RoomRequestAdapter(false) { _, _ -> }
        rvRequests.adapter = adapter

        fetchMyRequests()
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener {
                currentUserName = it.getString("name") ?: "Unknown"
            }
        }
    }

    private fun fetchMyRequests() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("room_requests")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { value, _ ->
                    val list = value?.toObjects(RoomRequest::class.java) ?: emptyList()
                    adapter.submitList(list)
                }
        }
    }
}

class RoomRequestAdapter(private val isWarden: Boolean, private val onAction: (RoomRequest, String) -> Unit) :
    RecyclerView.Adapter<RoomRequestAdapter.ViewHolder>() {

    private var items = listOf<RoomRequest>()

    fun submitList(newList: List<RoomRequest>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvRoomInfo.text = if (isWarden) "${item.userName}: ${item.acType}, ${item.sharing}" else "${item.acType}, ${item.sharing}"
        holder.tvStatus.text = "Status: ${item.status}"

        if (isWarden && item.status == "pending") {
            holder.layoutWardenActions.visibility = View.VISIBLE
            holder.btnApprove.setOnClickListener { onAction(item, "approved") }
            holder.btnReject.setOnClickListener { onAction(item, "rejected") }
        } else {
            holder.layoutWardenActions.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRoomInfo: TextView = view.findViewById(R.id.tvRoomInfo)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val layoutWardenActions: View = view.findViewById(R.id.layoutWardenActions)
        val btnApprove: Button = view.findViewById(R.id.btnApprove)
        val btnReject: Button = view.findViewById(R.id.btnReject)
    }
}
