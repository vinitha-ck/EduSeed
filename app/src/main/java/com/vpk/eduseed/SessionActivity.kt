package com.vpk.eduseed
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class SessionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var sessionAdapter: SessionAdapter
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private val sessionList = mutableListOf<Session>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // Display as Grid with 2 columns

        sessionAdapter = SessionAdapter(sessionList)
        recyclerView.adapter = sessionAdapter

        database = FirebaseDatabase.getInstance().reference
        sharedPreferences = getSharedPreferences("EduSeedPrefs", Context.MODE_PRIVATE)
        fetchSessions()
    }

    private fun fetchSessions() {
        val instructorName = sharedPreferences.getString("instructor_name", null) ?: return

        // Fetch Enrollments
        database.child("Enrollments").orderByChild("instructorName").equalTo(instructorName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    sessionList.clear()

                    for (enrollment in snapshot.children) {
                        val studentName = enrollment.child("studentName").value.toString()
                        val courseName = enrollment.child("courseName").value.toString()

                        // Fetch Updates
                        database.child("Updates").child(studentName)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(updateSnapshot: DataSnapshot) {
                                    val sessionCount = updateSnapshot.childrenCount.toInt()
                                    sessionList.add(Session(studentName, courseName, sessionCount.toString()))
                                    sessionAdapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Handle error
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }
}
