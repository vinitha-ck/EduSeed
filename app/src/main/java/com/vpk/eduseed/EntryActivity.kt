package com.vpk.eduseed

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class EntryActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageView
    private lateinit var btnEditEntries: ImageView
    private lateinit var topicEt: EditText
    private lateinit var homeworkEt: EditText
    private lateinit var dateEt: EditText
    private lateinit var timeEt: EditText
    private lateinit var submitBtn: Button
    private lateinit var database: DatabaseReference
    private var studentName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        backBtn = findViewById(R.id.back)
        topicEt = findViewById(R.id.topic)
        homeworkEt = findViewById(R.id.homework)
        dateEt = findViewById(R.id.date)
        timeEt = findViewById(R.id.time)
        submitBtn = findViewById(R.id.submit_button)
        btnEditEntries = findViewById(R.id.editEntry)

        studentName = intent.getStringExtra("student_name")
        findViewById<TextView>(R.id.profile_name).text = studentName ?: "Unknown"

        database = FirebaseDatabase.getInstance().reference

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateEt.setText(sdf.format(Date()))

        btnEditEntries.setOnClickListener {
            val intent = Intent(this, EditEntryActivity::class.java)
            intent.putExtra("studentName", studentName)
            startActivity(intent)
        }

        backBtn.setOnClickListener { finish() }

        submitBtn.setOnClickListener { checkAndSaveEntry() }
    }

    private fun getInstructorName(): String {
        val sharedPreferences = getSharedPreferences("EduSeedPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("instructor_name", "Unknown") ?: "Unknown"
    }

    private fun checkAndSaveEntry() {
        val topic = topicEt.text.toString().trim()
        val homework = homeworkEt.text.toString().trim()
        val date = dateEt.text.toString().trim()
        val time = timeEt.text.toString().trim()

        if (topic.isEmpty() || homework.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val studentRef = database.child("Updates").child(studentName!!).child(date)
        studentRef.get().addOnSuccessListener { snapshot ->
            var isDuplicate = false
            val entryCount = snapshot.childrenCount.toInt() + 1

            for (entry in snapshot.children) {
                val existingTopic = entry.child("topic").getValue(String::class.java)
                if (existingTopic == topic) {
                    isDuplicate = true
                    break
                }
            }

            if (isDuplicate) {
                Toast.makeText(this, "Entry already exists for this topic on this date.", Toast.LENGTH_SHORT).show()
            } else {
                saveEntry(studentRef, topic, homework, time, entryCount)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to check existing entries", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveEntry(studentRef: DatabaseReference, topic: String, homework: String, time: String, serialNo: Int) {
        val entryData = mapOf(
            "topic" to topic,
            "homework" to homework,
            "time" to time,
            "serialNo" to serialNo.toString(),
            "date" to dateEt.text.toString().trim()
        )

        studentRef.push().setValue(entryData)
            .addOnSuccessListener {
                Toast.makeText(this, "Entry Saved with Serial No: $serialNo", Toast.LENGTH_SHORT).show()
                saveToInstructorReport(serialNo, topic, homework, time)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToInstructorReport(serialNo: Int, topic: String, homework: String, time: String) {
        val instructorName = getInstructorName()
        val instructorRef = database.child("Instructor_Report").child(instructorName)

        val reportData = mapOf(
            "serialNo" to serialNo.toString(),
            "date" to dateEt.text.toString().trim(),
            "student_name" to studentName,
            "topic" to topic,
            "homework" to homework,
            "time" to time
        )

        instructorRef.push().setValue(reportData)
            .addOnSuccessListener {
                Toast.makeText(this, "Instructor Report Updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update Instructor Report", Toast.LENGTH_SHORT).show()
            }
    }
}
