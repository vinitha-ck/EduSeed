package com.vpk.eduseed

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.database.FirebaseDatabase

class AddFolderActivity : AppCompatActivity() {

    private lateinit var btnAddFolder: LinearLayout
    private lateinit var btnAccessAdmin: LinearLayout
    private lateinit var viewPager: ViewPager2
    private val handler = Handler()
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_folder)
        viewPager = findViewById(R.id.viewPager)

        val images = listOf(
            R.drawable.firstslide,
            R.drawable.secondslide,
            R.drawable.thirdslide,
            R.drawable.fourthslide,
            R.drawable.fifthslide
        )

        val adapter = ImageAdapter(this, images)
        viewPager.adapter = adapter

        val runnable = object : Runnable {
            override fun run() {
                if (currentPage == images.size) {
                    currentPage = 0
                }
                viewPager.setCurrentItem(currentPage++, true)
                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(runnable, 7000)
        btnAddFolder = findViewById(R.id.folder)
        btnAccessAdmin = findViewById(R.id.access_admin)
        // Find the buttons
        val btnReport = findViewById<LinearLayout>(R.id.report)
        val btnEnrollment = findViewById<LinearLayout>(R.id.enrollment)
        val stdEnrollment = findViewById<LinearLayout>(R.id.stud_enroll)
        val instructor_report = findViewById<LinearLayout>(R.id.instructor_report)
        // Navigate to ReportActivity
        btnReport.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
        }

        // Navigate to EnrollmentActivity
        btnEnrollment.setOnClickListener {
            val intent = Intent(this, Enrollment::class.java)
            startActivity(intent)
        }
        stdEnrollment.setOnClickListener {
            val intent = Intent(this, StudentActivity::class.java)
            startActivity(intent)
        }
        instructor_report.setOnClickListener {
            val intent = Intent(this, InstructorsReport::class.java)
            startActivity(intent)
        }
        btnAddFolder.setOnClickListener { showAddFolderDialog() }
        btnAccessAdmin.setOnClickListener {
            AccessAdmin().show(
                supportFragmentManager,
                "AccessAdmin"
            )
        }
    }

    private fun showAddFolderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_folder, null)
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

        val edtFolderName = dialogView.findViewById<EditText>(R.id.edtFolderName)
        val edtFolderLink = dialogView.findViewById<EditText>(R.id.edtFolderLink)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSubmit.setOnClickListener {
            val folderName = edtFolderName.text.toString().trim()
            val folderLink = edtFolderLink.text.toString().trim()

            if (folderName.isNotEmpty() && folderLink.isNotEmpty()) {
                val database = FirebaseDatabase.getInstance().getReference("Materials")
                database.child(folderName).setValue(folderLink)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Folder Added Successfully!", Toast.LENGTH_SHORT)
                            .show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to Add Folder", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter both fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
