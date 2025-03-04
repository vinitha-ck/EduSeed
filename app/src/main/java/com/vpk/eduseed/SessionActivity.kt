package com.vpk.eduseed

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SessionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        // Initialize RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns

        // Sample session data
        val sessionList = listOf(
            Session("Math Class", "Algebra Basics"),
            Session("Science Class", "Physics: Motion"),
            Session("English Class", "Grammar Basics"),
            Session("History Class", "World War II"),
        )

        // Set adapter
        val adapter = SessionAdapter(sessionList)
        recyclerView.adapter = adapter
    }
}
