package com.vpk.eduseed

import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class EditEntryActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var tableLayout: TableLayout
    private lateinit var btnUpdateAll: Button
    private var studentName: String? = null
    private val entryMap = mutableMapOf<String, EntryFields>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_entry)

        database = FirebaseDatabase.getInstance().reference.child("Updates")
        tableLayout = findViewById(R.id.tableLayout)
        btnUpdateAll = findViewById(R.id.saveButton)
        studentName = intent.getStringExtra("studentName")

        loadEntries()

        btnUpdateAll.setOnClickListener {
            updateAllEntries()
        }
    }

    private fun loadEntries() {
        studentName?.let { name ->
            val studentRef = database.child(name)

            studentRef.get().addOnSuccessListener { snapshot ->
                tableLayout.removeAllViews()
                entryMap.clear()

                if (!snapshot.exists()) {
                    Toast.makeText(this, "No entries found!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                addTableHeader() // Add table headers dynamically

                var serialNo = 1
                for (dateSnapshot in snapshot.children) {
                    val date = dateSnapshot.key ?: continue

                    for (entry in dateSnapshot.children) {
                        val entryId = entry.key ?: continue
                        val topic = entry.child("topic").value?.toString() ?: ""
                        val homework = entry.child("homework").value?.toString() ?: ""
                        val time = entry.child("time").value?.toString() ?: ""

                        val row = TableRow(this)

                        val serialNoEt = createEditText(serialNo.toString(), false)
                        val dateEt = createEditText(date, false)
                        val timeEt = createEditText(time)
                        val topicEt = createEditText(topic)
                        val homeworkEt = createEditText(homework)

                        row.addView(serialNoEt)
                        row.addView(dateEt)
                        row.addView(timeEt)
                        row.addView(topicEt)
                        row.addView(homeworkEt)

                        tableLayout.addView(row)
                        entryMap["$name/$date/$entryId"] = EntryFields(serialNoEt, dateEt, topicEt, homeworkEt, timeEt)

                        serialNo++
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAllEntries() {
        val updates = mutableMapOf<String, Any>()

        for ((key, fields) in entryMap) {
            val (serialNoEt, dateEt, topicEt, homeworkEt, timeEt) = fields

            val serialNo = serialNoEt.text.toString().trim()
            val date = dateEt.text.toString().trim()
            val topic = topicEt.text.toString().trim()
            val homework = homeworkEt.text.toString().trim()
            val time = timeEt.text.toString().trim()

            if (topic.isEmpty() || homework.isEmpty() || time.isEmpty()) {
                removeEntry(key)
            } else {
                updates["$key/serialNo"] = serialNo
                updates["$key/date"] = date
                updates["$key/topic"] = topic
                updates["$key/homework"] = homework
                updates["$key/time"] = time
            }
        }

        if (updates.isNotEmpty()) {
            database.updateChildren(updates).addOnSuccessListener {
                Toast.makeText(this, "All entries updated successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to update entries", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeEntry(entryPath: String) {
        database.child(entryPath).removeValue().addOnSuccessListener {
            Toast.makeText(this, "Removed empty entry", Toast.LENGTH_SHORT).show()
            loadEntries()
        }
    }


    private fun addTableHeader() {
        val headerRow = TableRow(this).apply {
            setBackgroundResource(R.drawable.toolbar_gradient)
            setPadding(8, 8, 8, 8)
        }

        val headers = listOf("S.No", "Date", "Time", "Topic", "Homework")

        headers.forEach { text ->
            val textView = TextView(this).apply {
                this.text = text
                setTextColor(resources.getColor(R.color.white, theme)) // White text
                setTypeface(null, Typeface.BOLD) // Bold text
                setPadding(8, 20, 8, 20)
                textSize = 16f
            }
            headerRow.addView(textView)
        }

        tableLayout.addView(headerRow)
    }


    private fun createEditText(text: String, editable: Boolean = true): EditText {
        return EditText(this).apply {
            setText(text)
            setPadding(10, 10, 10, 10)
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.cell_border)
            if (!editable) {
                inputType = InputType.TYPE_NULL
                isFocusable = false
            }
        }
    }

    private data class EntryFields(
        val serialNoEt: EditText,
        val dateEt: EditText,
        val topicEt: EditText,
        val homeworkEt: EditText,
        val timeEt: EditText
    )
}
