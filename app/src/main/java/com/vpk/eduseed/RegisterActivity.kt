package com.vpk.eduseed

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loginLayout: LinearLayout
    private lateinit var registerLayout: LinearLayout
    private lateinit var toggleText: TextView
    private lateinit var loginButton: Button
    private lateinit var saveButton: Button
    private lateinit var emailReg: EditText
    private lateinit var passwordReg: EditText
    private lateinit var number: EditText
    private lateinit var name: EditText
    private lateinit var emailLog: EditText
    private lateinit var passwordLog: EditText
    private lateinit var address: EditText
    private var isLoginMode = true
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("EduSeedPrefs", MODE_PRIVATE)

        // Check if user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        loginLayout = findViewById(R.id.login_layout)
        registerLayout = findViewById(R.id.register_layout)
        toggleText = findViewById(R.id.toggleText)
        loginButton = findViewById(R.id.login_Button)
        saveButton = findViewById(R.id.save_Button)
        emailReg = findViewById(R.id.email)
        passwordReg = findViewById(R.id.password)
        name = findViewById(R.id.name)
        number = findViewById(R.id.number)
        emailLog = findViewById(R.id.login_username)
        passwordLog = findViewById(R.id.login_password)
        address=findViewById(R.id.address)

        toggleText.setOnClickListener {
            toggleForms()
        }

        saveButton.setOnClickListener {
            val email = emailReg.text.toString().trim()
            val password = passwordReg.text.toString().trim()

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkIfEmailAuthorized(email, password)
        }

        loginButton.setOnClickListener {
            val email = emailLog.text.toString().trim()
            val password = passwordLog.text.toString().trim()

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveLoginStatus(email)
                        Toast.makeText(this, "Login success", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun saveLoginStatus(email: String) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.putString("userEmail", email)
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.child("instructors").child(userId).child("name").get()
                .addOnSuccessListener { snapshot ->
                    val instructorName = snapshot.getValue(String::class.java) ?: "Unknown"
                    editor.putString("instructor_name", instructorName)
                    editor.apply()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch instructor name", Toast.LENGTH_SHORT).show()
                    editor.apply()
                }
        } else {
            editor.apply()
        }
    }


    private fun checkIfEmailAuthorized(email: String, password: String) {
        val ref = database.child("Authorize")
        ref.get().addOnSuccessListener { snapshot ->
            var authorized = false
            var userRole: String? = null
            for (child in snapshot.children) {
                val storedEmail = child.child("email").getValue(String::class.java)
                if (storedEmail == email) {
                    authorized = true
                    userRole = child.child("role").getValue(String::class.java)
                    break
                }
            }
            if (authorized) {
                registerUser(email, password, userRole ?: "user")
            } else {
                Toast.makeText(this, "Email not authorized for registration", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to connect to database", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerUser(email: String, password: String, role: String) {
        val userName = name.text.toString().trim()
        val userNumber = number.text.toString().trim()
        val city=address.text.toString().trim()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        database.child("instructors").child(it).setValue(
                            mapOf(
                                "email" to email,
                                "role" to role,
                                "password" to password,
                                "number" to userNumber,
                                "name" to userName,
                                "city" to city
                            )
                        )
                    }
                    saveLoginStatus(email)
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
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

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}
