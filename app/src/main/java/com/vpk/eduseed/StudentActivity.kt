package com.vpk.eduseed

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class StudentActivity : AppCompatActivity() {

    private lateinit var studentName: EditText
    private lateinit var parentName: EditText
    private lateinit var city: EditText
    private lateinit var phone: EditText
    private lateinit var age: EditText
    private lateinit var grade: EditText
    private lateinit var submitButton: Button
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        databaseReference = FirebaseDatabase.getInstance().getReference("students")
        studentName = findViewById(R.id.studentname)
        parentName = findViewById(R.id.parentName)
        city = findViewById(R.id.country)
        phone = findViewById(R.id.phone)
        age = findViewById(R.id.age)
        grade = findViewById(R.id.grade)
        submitButton = findViewById(R.id.submit_button)
        submitButton.setOnClickListener {
            saveStudentData()
        }
    }

    private fun saveStudentData() {
        val name = studentName.text.toString().trim()
        val parent = parentName.text.toString().trim()
        val cityValue = city.text.toString().trim()
        val phoneValue = phone.text.toString().trim()
        val ageValue = age.text.toString().toIntOrNull() ?: 0
        val gradeValue = grade.text.toString().toIntOrNull() ?: 0

        if (TextUtils.isEmpty(name)) {
            studentName.error = "Enter student name"
            return
        }

        val student = Student(parent, cityValue, phoneValue, ageValue, gradeValue)

        // Store data in Firebase with student name as the key
        databaseReference.child(name).setValue(student)
            .addOnSuccessListener {
                Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add student", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        studentName.text.clear()
        parentName.text.clear()
        city.text.clear()
        phone.text.clear()
        age.text.clear()
        grade.text.clear()
    }
}
