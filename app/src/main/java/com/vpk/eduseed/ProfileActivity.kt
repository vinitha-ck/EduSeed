package com.vpk.eduseed

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var coursesListLayout: LinearLayout
    private lateinit var studentsListLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val profileName = findViewById<TextView>(R.id.profile_name)
        val profilePhone = findViewById<TextView>(R.id.phone)
        val profileEmail = findViewById<TextView>(R.id.email)
        val profileAddress = findViewById<TextView>(R.id.address1)
        coursesListLayout = findViewById(R.id.courses_list)
        studentsListLayout = findViewById(R.id.students_list)
        val backButton = findViewById<ImageView>(R.id.back)

        // Handle Back Button
        backButton.setOnClickListener { finish() }

        sharedPreferences = getSharedPreferences("EduSeedPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("userEmail", null)?.lowercase()

        if (userEmail == null) {
            Log.e("Profile", "No user email found in SharedPreferences")
            return
        }

        database = FirebaseDatabase.getInstance().reference

        // Fetch instructor details
        database.child("instructors").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (instructor in snapshot.children) {
                    val email = instructor.child("email").getValue(String::class.java)?.lowercase()
                    if (email == userEmail) {  // Case-insensitive comparison

                        val name = instructor.child("name").getValue(String::class.java)
                        val city = instructor.child("city").getValue(String::class.java)
                        val phone = instructor.child("number").getValue(String::class.java)

                        profileName.text = name ?: "N/A"
                        profileAddress.text = "City: ${city ?: "N/A"}"
                        profilePhone.text = "Mobile: ${phone ?: "N/A"}"
                        profileEmail.text = "Email: ${email ?: "N/A"}"

                        Log.d("Profile", "User found: $name, $city, $phone, $email")

                        name?.let { fetchEnrollments(it) }
                        return
                    }
                }
                Log.e("Profile", "No matching user found in database")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Profile", "Database error: ${error.message}")
            }
        })
    }

    private fun fetchEnrollments(instructorName: String) {
        database.child("Enrollments").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courses = mutableSetOf<String>()
                val students = mutableSetOf<String>()

                for (enrollment in snapshot.children) {
                    val instructor = enrollment.child("instructorName").getValue(String::class.java)
                    if (instructor?.equals(instructorName, ignoreCase = true) == true) {
                        val course = enrollment.child("courseName").getValue(String::class.java)
                        val student = enrollment.child("studentName").getValue(String::class.java)

                        course?.let { courses.add(it) }
                        student?.let { students.add(it) }
                    }
                }

                if (courses.isEmpty()) courses.add("No courses found")
                if (students.isEmpty()) students.add("No students found")

                // Display lists in UI
                displayList(courses.toList(), coursesListLayout)
                displayList(students.toList(), studentsListLayout)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Profile", "Error fetching enrollments: ${error.message}")
            }
        })
    }

    private fun displayList(items: List<String>, layout: LinearLayout) {
        layout.removeAllViews()
        for (item in items) {
            val textView = TextView(this)
            textView.text = item
            textView.typeface = ResourcesCompat.getFont(this, R.font.poppins)
            textView.textSize = 15f
            textView.setPadding(10, 5, 10, 10)
            layout.addView(textView)
        }
    }
}
