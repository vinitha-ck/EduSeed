package com.vpk.eduseed

import android.app.AlertDialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var btnAddFolder: Button
    private lateinit var btnAccessAdmin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAddFolder = findViewById(R.id.btnAddFolder)
        btnAccessAdmin = findViewById(R.id.btnAccessAdmin)

        database = FirebaseDatabase.getInstance().reference

        btnAddFolder.setOnClickListener { showAddFolderDialog() }
        btnAccessAdmin.setOnClickListener { showAddFolderDialog() }
    }

    // Function to show Folder Adding Dialog
    private fun showAddFolderDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Folder")

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_folder, null)
        builder.setView(dialogView)

        val edtFolderName = dialogView.findViewById<EditText>(R.id.edtFolderName)
        val edtFolderLink = dialogView.findViewById<EditText>(R.id.edtFolderLink)

        builder.setPositiveButton("Submit") { dialog, _ ->
            val folderName = edtFolderName.text.toString().trim()
            val folderLink = edtFolderLink.text.toString().trim()

            if (folderName.isNotEmpty() && folderLink.isNotEmpty()) {
                database.child("Materials").child(folderName).setValue(folderLink)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Folder Added Successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to Add Folder", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter both fields!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    // Function to show Admin Authorization Dialog
    private fun showAccessAdminDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Grant Access")

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_folder, null)
        builder.setView(dialogView)

        val edtEmail = dialogView.findViewById<EditText>(R.id.edtFolderName)
        val edtRole = dialogView.findViewById<EditText>(R.id.edtFolderLink)

        builder.setPositiveButton("Admit") { dialog, _ ->
            val email = edtEmail.text.toString().trim()
            val role = edtRole.text.toString().trim()

            if (email.isNotEmpty() && role.isNotEmpty()) {
                saveEmailToDatabase(email, role)
            } else {
                Toast.makeText(this, "Enter both email and role", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun saveEmailToDatabase(email: String, role: String) {
        val ref = database.child("Authorize")

        ref.get().addOnSuccessListener { snapshot ->
            val newKey = "auth_${snapshot.childrenCount + 1}"

            val userEntry = mapOf(
                "email" to email,
                "role" to role
            )

            ref.child(newKey).setValue(userEntry)
                .addOnSuccessListener {
                    Log.d("Firebase", "Email and role added successfully!")
                    SendEmailTask().execute(email)
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to add email and role", e)
                }
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Failed to fetch data", e)
        }
    }

    inner class SendEmailTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg params: String?): Boolean {
            return try {
                val sender = GMailSender("vinitha5314@gmail.com", "quev tvrc vvjh rhdd")
                sender.sendMail(params[0]!!, "Access Granted",
                    "Access granted! You now have permission to use our application.\n\n" +
                            "Download the app here: https://play.google.com/store/apps/details?id=com.example.app\n\n" +
                            "Please register and set your password, then sign in to the app or use Google Sign-In for quick access.\n\n" +
                            "If you have any doubts, feel free to reach out to us at eduseed@gmail.com.")

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                Toast.makeText(this@MainActivity, "Email Sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Failed to send email", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
