package com.vpk.eduseed

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.database.*

class InstructorsReport : AppCompatActivity() {
    private lateinit var instructorAutoComplete: AutoCompleteTextView
    private lateinit var tableLayout: TableLayout
    private lateinit var saveButton: Button
    private lateinit var dbRef: DatabaseReference
    private lateinit var instructorReportRef: DatabaseReference
    private val instructorList = mutableListOf<String>()
    private val entryMap = mutableMapOf<String, Quintuple<EditText, EditText, EditText, EditText, EditText>>()
    private var selectedInstructor = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructor_report)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        instructorAutoComplete = findViewById(R.id.studentAutoComplete)
        tableLayout = findViewById(R.id.tableLayout)
        saveButton = findViewById(R.id.saveButton)

        dbRef = FirebaseDatabase.getInstance().getReference("instructors")
        instructorReportRef = FirebaseDatabase.getInstance().getReference("Instructor_Report")

        loadInstructorNames()

        instructorAutoComplete.setOnItemClickListener { parent, _, position, _ ->
            selectedInstructor = parent.getItemAtPosition(position).toString()
            fetchInstructorEntries(selectedInstructor)
        }

        saveButton.setOnClickListener { saveEditedEntries() }
    }

    private fun loadInstructorNames() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                instructorList.clear()
                for (instructorSnapshot in snapshot.children) {
                    val name = instructorSnapshot.child("name").getValue(String::class.java)
                    name?.let { instructorList.add(it) }
                }
                val adapter = ArrayAdapter(this@InstructorsReport, android.R.layout.simple_dropdown_item_1line, instructorList)
                instructorAutoComplete.setAdapter(adapter)
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error: ${error.message}")
            }
        })
    }

    private fun fetchInstructorEntries(instructorName: String) {
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }
        entryMap.clear()

        instructorReportRef.child(instructorName).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var serial = 1
                for (entrySnapshot in snapshot.children) {
                    val entryId = entrySnapshot.key ?: continue
                    val date = entrySnapshot.child("date").getValue(String::class.java)
                    val time = entrySnapshot.child("time").getValue(String::class.java)
                    val topic = entrySnapshot.child("topic").getValue(String::class.java)
                    val homework = entrySnapshot.child("homework").getValue(String::class.java)
                    val studentName = entrySnapshot.child("student_name").getValue(String::class.java)

                    val row = TableRow(this@InstructorsReport).apply { gravity = Gravity.CENTER_VERTICAL }

                    val serialEt = createEditText(serial.toString())
                    val dateEt = createEditText(date)
                    val timeEt = createEditText(time)
                    val studentEt = createEditText(studentName)
                    val topicEt = createEditText(topic)
                    val homeworkEt = createEditText(homework)

                    row.addView(serialEt)
                    row.addView(dateEt)
                    row.addView(timeEt)
                    row.addView(studentEt)
                    row.addView(topicEt)
                    row.addView(homeworkEt)

                    tableLayout.addView(row)

                    val pathKey = "$instructorName/$entryId"
                    entryMap[pathKey] = Quintuple(serialEt, dateEt, timeEt, topicEt, homeworkEt)

                    serial++
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error: ${error.message}")
            }
        })
    }

    private fun saveEditedEntries() {
        if (selectedInstructor.isEmpty()) {
            showToast("No instructor selected")
            return
        }
        val updates = mutableMapOf<String, Any>()

        for ((path, quintuple) in entryMap) {
            updates["$path/date"] = quintuple.second.text.toString()
            updates["$path/time"] = quintuple.third.text.toString()
            updates["$path/topic"] = quintuple.fourth.text.toString()
            updates["$path/homework"] = quintuple.fifth.text.toString()
        }

        instructorReportRef.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showToast("Entries updated successfully")
            } else {
                showToast("Failed to update entries")
            }
        }
    }

    private fun createEditText(text: String?): EditText {
        return EditText(this).apply {
            setText(text)
            setPadding(8, 8, 8, 8)
            setBackgroundResource(R.drawable.cell_border)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}