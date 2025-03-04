package com.vpk.eduseed
<<<<<<< HEAD
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
=======

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
>>>>>>> 58ce0f97a1132423c4135e97f3d15f6d82e8d5b0

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
<<<<<<< HEAD

        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val sendButton = findViewById<Button>(R.id.sendButton)

        sendButton.setOnClickListener {
            val recipientEmail = editTextEmail.text.toString().trim()
            if (recipientEmail.isNotEmpty()) {
                SendEmailTask().execute(recipientEmail)
            } else {
                Toast.makeText(this, "Enter an email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class SendEmailTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg params: String?): Boolean {
            return try {
                val sender = GMailSender("vinitha5314@gmail.com", "quev tvrc vvjh rhdd")
                sender.sendMail(params[0]!!, "Test Email", "Hello, this is a test email from Android App.")
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                Toast.makeText(applicationContext, "Email Sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Failed to send email", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
=======
    }
}
>>>>>>> 58ce0f97a1132423c4135e97f3d15f6d82e8d5b0
