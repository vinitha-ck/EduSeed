package com.vpk.eduseed
data class Instructor(
    val name: String = "",
    val email: String = "",
    val mobile: String = "",
    val city: String = "",
    val preferences: List<String> = emptyList()
)
