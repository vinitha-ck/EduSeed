package com.vpk.eduseed

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.FirebaseDatabase

class AccessAdmin : DialogFragment() {
    private lateinit var editTextEmail: EditText
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_access_admin, container, false)

        editTextEmail = view.findViewById(R.id.editTextSubtext)
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        cancelButton.setOnClickListener { dismiss() }
        saveButton.setOnClickListener {
            val recipientEmail = editTextEmail.text.toString().trim()
            val role = view.findViewById<EditText>(R.id.editTextRole).text.toString().trim()
            if (recipientEmail.isNotEmpty() && role.isNotEmpty()) {
                saveEmailToDatabase(recipientEmail, role)
            } else {
                Toast.makeText(requireContext(), "Enter both email and role", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    private fun saveEmailToDatabase(email: String, role: String) {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("Authorize")
        ref.get().addOnSuccessListener { snapshot ->
            val newKey = "auth_${snapshot.childrenCount + 1}" // Generate unique key
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


    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
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
                Toast.makeText(requireContext(), "Email Sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to send email", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
