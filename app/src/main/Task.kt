package com.vpk.eduseed
data class Task(
    var id: Long = 0,
    var text: String,
    var subtext: String,
    var time: String,
    var isChecked: Boolean
)