package com.vpk.eduseed

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.database.*

class ReportActivity : AppCompatActivity() {

    private lateinit var studentAutoComplete: AutoCompleteTextView
    private lateinit var tableLayout: TableLayout
    private lateinit var saveButton: Button

    // Firebase reference to "Updates"
    private lateinit var dbRef: DatabaseReference

    // List of student names (keys under "Updates")
    private val studentList = mutableListOf<String>()

    // Map to store references for each entry’s editable fields.
    // Key format: "studentName/date/entryId"
    // Value holds a Quintuple for: (S.No, Date, Time, Topic, Homework)
    private val entryMap = mutableMapOf<String, Quintuple<EditText, EditText, EditText, EditText, EditText>>()

    // Currently selected student name
    private var selectedStudent = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        studentAutoComplete = findViewById(R.id.studentAutoComplete)
        tableLayout = findViewById(R.id.tableLayout)
        saveButton = findViewById(R.id.saveButton)

        // Initialize Firebase reference to "Updates"
        dbRef = FirebaseDatabase.getInstance().getReference("Updates")

        // Load student names from Firebase
        loadStudentNames()

        // When a student is selected, fetch that student's entries
        studentAutoComplete.setOnItemClickListener { parent, _, position, _ ->
            selectedStudent = parent.getItemAtPosition(position) as String
            fetchStudentEntries(selectedStudent)
        }


        saveButton.setOnClickListener {
            saveEditedEntries()
        }
    }

    private fun loadStudentNames() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                studentList.clear()
                for (studentSnapshot in snapshot.children) {
                    studentSnapshot.key?.let { studentList.add(it) }
                }
                val adapter = ArrayAdapter(
                    this@ReportActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    studentList
                )
                studentAutoComplete.setAdapter(adapter)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ReportActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("ReportActivity", "loadStudentNames: ${error.message}")
            }
        })
    }

    /**
     * Fetches all entries for the selected student and populates the table.
     * Firebase structure assumed:
     * Updates -> studentName -> date -> entryId -> { date, time, topic, homework }
     */
    private fun fetchStudentEntries(studentName: String) {
        // Remove any rows except the header (which is at index 0)
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }
        entryMap.clear()

        val studentRef = dbRef.child(studentName)
        studentRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var serial = 1
                // Loop through each date node under the student
                for (dateSnapshot in snapshot.children) {
                    val dateKey = dateSnapshot.key ?: continue
                    // Loop through each entry under the date
                    for (entrySnapshot in dateSnapshot.children) {
                        val entryId = entrySnapshot.key ?: continue
                        val dateVal = entrySnapshot.child("date").value?.toString() ?: dateKey
                        val timeVal = entrySnapshot.child("time").value?.toString() ?: ""
                        val topicVal = entrySnapshot.child("topic").value?.toString() ?: ""
                        val homeworkVal = entrySnapshot.child("homework").value?.toString() ?: ""

                        // Create a new TableRow for this entry
                        val row = TableRow(this@ReportActivity).apply {
                            layoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                            )
                            gravity = Gravity.CENTER_VERTICAL
                        }

                        // S.No field (editable if needed)
                        val serialEt = EditText(this@ReportActivity).apply {
                            setText(serial.toString())
                            setPadding(8, 8, 8, 8)
                            setBackgroundResource(R.drawable.cell_border)
                        }
                        row.addView(serialEt)

                        // Date field
                        val dateEt = EditText(this@ReportActivity).apply {
                            setText(dateVal)
                            setPadding(8, 8, 8, 8)
                            setBackgroundResource(R.drawable.cell_border)
                        }
                        row.addView(dateEt)

                        // Time field
                        val timeEt = EditText(this@ReportActivity).apply {
                            setText(timeVal)
                            setPadding(8, 8, 8, 8)
                            setBackgroundResource(R.drawable.cell_border)
                        }
                        row.addView(timeEt)

                        // Topic field
                        val topicEt = EditText(this@ReportActivity).apply {
                            setText(topicVal)
                            setPadding(8, 8, 8, 8)
                            setBackgroundResource(R.drawable.cell_border)
                        }
                        row.addView(topicEt)

                        // Homework field
                        val homeworkEt = EditText(this@ReportActivity).apply {
                            setText(homeworkVal)
                            setPadding(8, 8, 8, 8)
                            setBackgroundResource(R.drawable.cell_border)
                        }
                        row.addView(homeworkEt)

                        // Add the row to the table
                        tableLayout.addView(row)

                        // Build a unique key for this entry: "studentName/date/entryId"
                        val pathKey = "$studentName/$dateKey/$entryId"
                        // Save the references so we can update them later
                        entryMap[pathKey] = Quintuple(serialEt, dateEt, timeEt, topicEt, homeworkEt)

                        serial++
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ReportActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("ReportActivity", "fetchStudentEntries: ${error.message}")
            }
        })
    }

    /**
     * Reads all edited values from the table and updates Firebase.
     */
    private fun saveEditedEntries() {
        if (selectedStudent.isEmpty()) {
            Toast.makeText(this, "No student selected", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mutableMapOf<String, Any>()
        for ((path, quintuple) in entryMap) {
            val (serialEt, dateEt, timeEt, topicEt, homeworkEt) = quintuple
            val newSerial = serialEt.text.toString().trim()
            val newDate = dateEt.text.toString().trim()
            val newTime = timeEt.text.toString().trim()
            val newTopic = topicEt.text.toString().trim()
            val newHomework = homeworkEt.text.toString().trim()

            // Construct update keys (e.g., "studentName/date/entryId/date", etc.)
            updates["$path/serialNo"] = newSerial
            updates["$path/date"] = newDate
            updates["$path/time"] = newTime
            updates["$path/topic"] = newTopic
            updates["$path/homework"] = newHomework
        }

        if (updates.isNotEmpty()) {
            dbRef.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Entries updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update entries", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "No changes to update", Toast.LENGTH_SHORT).show()
        }
    }

    // Data class to hold five EditText references (S.No, Date, Time, Topic, Homework)
    private data class Quintuple<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )
}
