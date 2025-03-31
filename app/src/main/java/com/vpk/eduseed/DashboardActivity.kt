package com.vpk.eduseed

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var navigationView: NavigationView
    private lateinit var database: DatabaseReference
    private lateinit var adapter: UpcomingClassesAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var name: TextView
    private var userRole: String = "Tutor"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("Authorize")
        sharedPreferences = getSharedPreferences("EduSeedPrefs", MODE_PRIVATE)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val sharedPreferences = getSharedPreferences("EduSeedPrefs", MODE_PRIVATE)
        val instructorName = sharedPreferences.getString("instructor_name", "user")
        tvGreeting.text = "Hello, $instructorName"
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UpcomingClassesAdapter(emptyList())
        recyclerView.adapter = adapter

        fetchTodayEnrollments()
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        checkUserRole()
        updateNavHeader()
    }
    private fun fetchTodayEnrollments() {
        val today = getCurrentDay()
        val currentInstructorName = sharedPreferences.getString("instructor_name", "") ?: ""
        val enrollmentsRef = FirebaseDatabase.getInstance().getReference("Enrollments")
        enrollmentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val todayEnrollments = mutableListOf<EnrollmentData>()
                for (enrollmentSnapshot in snapshot.children) {
                    val enrollment = enrollmentSnapshot.getValue(EnrollmentData::class.java)
                    if (enrollment != null) {
                        if (enrollment.instructorName.equals(currentInstructorName, ignoreCase = true)
                            && enrollment.selectedDays.contains(today)
                        ) {
                            todayEnrollments.add(enrollment)
                        }
                    }
                }
                // Update the adapter's list (or show a message if empty)
                if (todayEnrollments.isEmpty()) {
                    Toast.makeText(this@DashboardActivity, "No classes for today", Toast.LENGTH_SHORT).show()
                }
                adapter.updateList(todayEnrollments)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UpcomingClasses", "Error fetching enrollments: ${error.message}")
            }
        })
    }

    private fun getCurrentDay(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Unknown"
        }
    }
    private fun updateNavHeader() {
        val userEmail = sharedPreferences.getString("userEmail", null)
        if (userEmail != null) {
            val instructorsRef = FirebaseDatabase.getInstance().reference.child("instructors")
            instructorsRef.orderByChild("email").equalTo(userEmail)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                val username = child.child("name").getValue(String::class.java) ?: "Username"
                                val headerView = navigationView.getHeaderView(0)
                                val navUsernameTextView = headerView.findViewById<TextView>(R.id.nav_username)
                                navUsernameTextView.text = username
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle possible errors
                    }
                })
        }
    }


    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        } else {
            // User is signed in, proceed with fetching data or other operations
            fetchTodayEnrollments()
            updateNavHeader()
        }
    }


    private fun checkUserRole() {
        val userEmail = sharedPreferences.getString("userEmail", null)
        if (userEmail != null) {
            database.orderByChild("email").equalTo(userEmail)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                userRole = child.child("role").getValue(String::class.java) ?: "Tutor"
                                updateNavigationMenu()
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }

    private fun updateNavigationMenu() {
        val menu: Menu = navigationView.menu
        menu.clear()
        menu.add(Menu.NONE, R.id.nav_home, Menu.NONE, "Home").setIcon(R.drawable.home)
        menu.add(Menu.NONE, R.id.nav_profile, Menu.NONE, "Profile").setIcon(R.drawable.baseline_person_24)
        menu.add(Menu.NONE, R.id.nav_material, Menu.NONE, "Materials").setIcon(R.drawable.baseline_library_books_24)
        menu.add(Menu.NONE, R.id.nav_entry, Menu.NONE, "Entry").setIcon(R.drawable.baseline_edit_note_24)
        menu.add(Menu.NONE, R.id.nav_session, Menu.NONE, "Sessions").setIcon(R.drawable.baseline_collections_bookmark_24)
        if (userRole.equals("admin", ignoreCase = true)) {
            menu.add(Menu.NONE, R.id.nav_admin, Menu.NONE, "Admin Panel").setIcon(R.drawable.baseline_admin_panel_settings_24)
        }
        menu.add(Menu.NONE, R.id.nav_log_out, Menu.NONE, "Log Out").setIcon(R.drawable.baseline_logout_24)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> startActivity(Intent(this,DashboardActivity::class.java))
            R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
            R.id.nav_material -> startActivity(Intent(this, DriveActivity::class.java))
            R.id.nav_entry -> startActivity(Intent(this, SampleActivity::class.java))
            R.id.nav_session -> startActivity(Intent(this, SessionActivity::class.java))
            R.id.nav_admin -> startActivity(Intent(this, AddFolderActivity::class.java))
            R.id.nav_log_out -> logoutUser()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logoutUser() {
        auth.signOut()
        sharedPreferences.edit().clear().apply()
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }
}
