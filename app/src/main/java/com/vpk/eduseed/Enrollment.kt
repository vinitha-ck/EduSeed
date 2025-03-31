package com.vpk.eduseed
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.*

class Enrollment : AppCompatActivity() {

    private lateinit var studentName: EditText
    private lateinit var instructorName: EditText
    private lateinit var classTime: EditText
    private lateinit var courseName: AutoCompleteTextView
    private lateinit var batchTypeDropdown: AutoCompleteTextView
    private lateinit var submitButton: Button
    private lateinit var databaseReference: DatabaseReference
    private lateinit var coursesRef: DatabaseReference
    private lateinit var batchRef: DatabaseReference
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var selectedDays: BooleanArray
    private val selectedDaysList = ArrayList<String?>()

    private val days = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enrollment)

        databaseReference = FirebaseDatabase.getInstance().getReference("Enrollments")
        coursesRef = FirebaseDatabase.getInstance().getReference("courses")
        batchRef = FirebaseDatabase.getInstance().getReference("Batch")

        studentName = findViewById(R.id.studentname)
        instructorName = findViewById(R.id.instructorname)
        courseName = findViewById(R.id.courses_list)
        classTime = findViewById(R.id.time)
        batchTypeDropdown = findViewById(R.id.batchTypeDropdown)
        submitButton = findViewById(R.id.submit_button)
        autoCompleteTextView = findViewById(R.id.editTextSelectDays)
        selectedDays = BooleanArray(days.size)

        fetchCourses()
        fetchBatchTypes()

        courseName.setOnClickListener { courseName.showDropDown() }
        batchTypeDropdown.setOnClickListener { batchTypeDropdown.showDropDown() }

        autoCompleteTextView.setOnClickListener { showDaysDialog() }

        submitButton.setOnClickListener { saveEnrollmentData() }
    }

    private fun fetchCourses() {
        coursesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val coursesList = mutableListOf<String>()
                for (courseSnapshot in snapshot.children) {
                    val courseName = courseSnapshot.getValue(String::class.java)
                    courseName?.let { coursesList.add(it) }
                }
                val adapter = ArrayAdapter(this@Enrollment, android.R.layout.simple_dropdown_item_1line, coursesList)
                courseName.setAdapter(adapter)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Enrollment, "Failed to load courses", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchBatchTypes() {
        batchRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val batchList = mutableListOf<String>()
                for (batchSnapshot in snapshot.children) {
                    val batchType = batchSnapshot.getValue(String::class.java)
                    batchType?.let { batchList.add(it) }
                }
                val adapter = ArrayAdapter(this@Enrollment, android.R.layout.simple_dropdown_item_1line, batchList)
                batchTypeDropdown.setAdapter(adapter)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Enrollment, "Failed to load batch types", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDaysDialog() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Select Days")
        builder.setMultiChoiceItems(days, selectedDays) { _, which, isChecked ->
            if (isChecked) {
                selectedDaysList.add(days[which])
            } else {
                selectedDaysList.remove(days[which])
            }
        }
        builder.setPositiveButton("OK") { _, _ ->
            autoCompleteTextView.setText(selectedDaysList.joinToString(", "))
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun saveEnrollmentData() {
        val student = studentName.text.toString().trim()
        val instructor = instructorName.text.toString().trim()
        val course = courseName.text.toString().trim()
        val time = classTime.text.toString().trim()
        val batchType = batchTypeDropdown.text.toString().trim()

        if (student.isEmpty() || instructor.isEmpty() || course.isEmpty() || time.isEmpty() || batchType.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val enrollmentId = databaseReference.push().key
        val enrollmentData = hashMapOf(
            "studentName" to student,
            "instructorName" to instructor,
            "courseName" to course,
            "classTime" to time,
            "batchType" to batchType,
            "selectedDays" to selectedDaysList
        )

        enrollmentId?.let {
            databaseReference.child(it).setValue(enrollmentData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Enrollment Successful!", Toast.LENGTH_SHORT).show()
                    clearFields()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to Enroll: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun clearFields() {
        studentName.text.clear()
        instructorName.text.clear()
        courseName.text.clear()
        classTime.text.clear()
        batchTypeDropdown.text.clear()
        autoCompleteTextView.text.clear()
        selectedDaysList.clear()
        selectedDays.fill(false)
    }
}
