package com.example.smarthostel

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val year: String = "",
    val department: String = "",
    val roomNumber: String = "",
    val phoneNumber: String = "",
    val role: String = "Student"
)

data class LeaveRequest(
    val id: String = "",
    val userId: String = "",
    val fromDate: String = "",
    val toDate: String = "",
    val reason: String = "",
    var status: String = "pending",
    val userName: String = "" // Moved to end to prevent positional shifting
)

data class Complaint(
    val id: String = "",
    val userId: String = "",
    val category: String = "",
    val description: String = "",
    var status: String = "pending",
    val userName: String = ""
)

data class FoodReview(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val userName: String = ""
)

data class RoomRequest(
    val id: String = "",
    val userId: String = "",
    val acType: String = "",
    val sharing: String = "",
    var status: String = "pending",
    val userName: String = ""
)
