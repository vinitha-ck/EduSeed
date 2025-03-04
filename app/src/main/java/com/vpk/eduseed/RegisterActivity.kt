package com.vpk.eduseed

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var loginLayout: LinearLayout
    private lateinit var registerLayout: LinearLayout
    private lateinit var toggleText: TextView
    private lateinit var loginButton: Button
    private lateinit var saveButton: Button
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        loginLayout = findViewById(R.id.login_layout)
        registerLayout = findViewById(R.id.register_layout)
        toggleText = findViewById(R.id.toggleText)
        loginButton = findViewById(R.id.login_Button)
        saveButton = findViewById(R.id.save_Button)

        toggleText.setOnClickListener {
            toggleForms()
        }

        loginButton.setOnClickListener {
            navigateToDashboard()
        }

        saveButton.setOnClickListener {
            navigateToDashboard()
        }
    }

    private fun toggleForms() {
        if (isLoginMode) {
            loginLayout.visibility = View.GONE
            registerLayout.visibility = View.VISIBLE
            toggleText.text = "Already have an account? Login"
        } else {
            loginLayout.visibility = View.VISIBLE
            registerLayout.visibility = View.GONE
            toggleText.text = "New user? Register"
        }
        isLoginMode = !isLoginMode
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}
