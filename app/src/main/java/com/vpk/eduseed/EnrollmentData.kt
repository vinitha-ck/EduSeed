package com.vpk.eduseed

data class EnrollmentData(
    val studentName: String = "",
    val instructorName: String = "",
    val courseName: String = "",
    val classTime: String = "",
    val batchType: String = "",
    val selectedDays: List<String> = emptyList()
)
