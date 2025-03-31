package com.vpk.eduseed

import android.app.AlertDialog
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class SampleActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var entryAdapter: EntryAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private val enrollmentsRef = FirebaseDatabase.getInstance().getReference("Enrollments")
    private val entryRef = FirebaseDatabase.getInstance().getReference("Entry")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        entryAdapter = EntryAdapter(mutableListOf())
        recyclerView.adapter = entryAdapter

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            showAddEntryDialog()
        }

        sharedPreferences = getSharedPreferences("EduSeedPrefs", MODE_PRIVATE)
        val instructorName = sharedPreferences.getString("instructor_name", null)

        if (instructorName.isNullOrEmpty()) {
            Log.e("CheckInstructor", "No instructor name found in SharedPreferences")
        } else {
            Log.d("CheckInstructor", "Instructor name found: $instructorName")
        }

        loadEntries()
    }

    private fun showAddEntryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.activity_sample_folder, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val edtStudent = dialogView.findViewById<EditText>(R.id.student)
        val edtCourse = dialogView.findViewById<EditText>(R.id.course)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSubmit.setOnClickListener {
            val studentName = edtStudent.text.toString().trim()
            val courseName = edtCourse.text.toString().trim()

            if (studentName.isNotEmpty() && courseName.isNotEmpty()) {
                checkEnrollmentAndAddEntry(studentName, courseName, dialog)
            } else {
                Toast.makeText(this, "Please enter both fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkEnrollmentAndAddEntry(studentName: String, courseName: String, dialog: AlertDialog) {
        sharedPreferences = getSharedPreferences("EduSeedPrefs", MODE_PRIVATE)
        val instructorName = sharedPreferences.getString("instructor_name", null)

        if (instructorName.isNullOrEmpty()) {
            Toast.makeText(this, "Instructor name not found.", Toast.LENGTH_SHORT).show()
            return
        }

        enrollmentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var exists = false

                for (child in snapshot.children) {
                    val storedInstructor = child.child("instructorName").value?.toString()
                    val storedStudent = child.child("studentName").value?.toString()
                    val storedCourse = child.child("courseName").value?.toString()
                    if (storedInstructor == instructorName && storedStudent == studentName && storedCourse == courseName) {
                        exists = true
                        break
                    }
                }

                if (exists) {
                    entryRef.orderByChild("student").equalTo(studentName).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(entrySnapshot: DataSnapshot) {
                            var alreadyExists = false
                            for (entry in entrySnapshot.children) {
                                val storedCourse = entry.child("course").value?.toString()
                                val storedInstructor = entry.child("instructor").value?.toString()

                                if (storedCourse == courseName && storedInstructor == instructorName) {
                                    alreadyExists = true
                                    break
                                }
                            }

                            if (!alreadyExists) {
                                val entryId = entryRef.push().key
                                if (entryId != null) {
                                    val entryData = mapOf(
                                        "student" to studentName,
                                        "course" to courseName,
                                        "instructor" to instructorName
                                    )

                                    entryRef.child(entryId).setValue(entryData)
                                        .addOnSuccessListener {
                                            Toast.makeText(applicationContext, "Entry Added Successfully!", Toast.LENGTH_SHORT).show()
                                            entryAdapter.addEntry(Entry(studentName, courseName, instructorName))
                                            dialog.dismiss()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(applicationContext, "Failed to Add Entry", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            } else {
                                Toast.makeText(applicationContext, "Entry already exists!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(applicationContext, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(applicationContext, "No matching enrollment found!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun loadEntries() {
        sharedPreferences = getSharedPreferences("EduSeedPrefs", MODE_PRIVATE)
        val instructorName = sharedPreferences.getString("instructor_name", null)

        if (instructorName.isNullOrEmpty()) {
            Toast.makeText(this, "Instructor name not found.", Toast.LENGTH_SHORT).show()
            return
        }

        entryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filteredEntries = mutableListOf<Entry>()
                val seenEntries = HashSet<String>() // Track duplicates

                for (child in snapshot.children) {
                    val student = child.child("student").value?.toString() ?: ""
                    val course = child.child("course").value?.toString() ?: ""
                    val instructor = child.child("instructor").value?.toString() ?: ""

                    val entryKey = "$student|$course|$instructor" // Unique key for checking duplicates

                    if (instructor == instructorName && !seenEntries.contains(entryKey)) {
                        seenEntries.add(entryKey)
                        filteredEntries.add(Entry(student, course, instructor))
                    }
                }
                entryAdapter.updateEntries(filteredEntries)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Failed to load entries.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
