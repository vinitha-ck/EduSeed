package com.vpk.eduseed

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SecondActivity : AppCompatActivity(), ThirdDialogFragment.ThirdDialogListener {
    private var dataList = ArrayList<Task>()
    private lateinit var recyclerView: RecyclerView
    private var adapter: TaskAdapter? = null
    private var dataSource: DataSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        dataSource = DataSource(this)
        dataList = dataSource!!.getAllAlarms() as ArrayList<Task> // Fetch all tasks from the database

        recyclerView = findViewById(R.id.recyclerView)
        adapter = TaskAdapter(this, dataList, dataSource!!) // Pass the DataSource to the adapter
        recyclerView.adapter = adapter

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
          //  val dialog = ThirdDialogFragment()
          //  dialog.show(supportFragmentManager, "ThirdDialogFragment")
            val dialog = AccessAdmin()
            dialog.show(supportFragmentManager, "AccessAdminDialog")
        }
    }
    override fun onTaskSaved(text: String, subtext: String) {
        val newTask = Task(text, subtext)
        val id = dataSource?.addTask(newTask) ?: -1L
        if (id != -1L) {
            newTask.id = id
            dataList.add(newTask)
            adapter?.notifyItemInserted(dataList.size - 1)
        }
    }
    fun showPopupMenu(view: View) {
        recyclerView.let { rv ->  // Ensures recyclerView is initialized
            val position = rv.getChildLayoutPosition(view.parent as View)
            val popupMenu = PopupMenu(this, view)
            popupMenu.inflate(R.menu.popup_menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_entry -> {
                        handleEntryAction(position)
                        true
                    }
                    R.id.menu_edit -> {
                        handleEditAction(position)
                        true
                    }
                    R.id.menu_delete -> {
                        handleDeleteAction(position)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }
    private fun handleEntryAction(position: Int) {
        val task = dataList[position]
        val intent = Intent(this@SecondActivity, EntryActivity::class.java).apply {
            putExtra("text", task.text)
            putExtra("subtext", task.subtext)
            putExtra("position", position)
            putExtra("id", task.id)
        }
        startActivityForResult(intent, EDIT_REQUEST_CODE)
    }
    private fun handleEditAction(position: Int) {
        val task = dataList[position]
        val intent = Intent(this@SecondActivity, EntryActivity::class.java).apply {
            putExtra("text", task.text)
            putExtra("subtext", task.subtext)
            putExtra("position", position)
            putExtra("id", task.id)
        }
        startActivityForResult(intent, EDIT_REQUEST_CODE)
    }

    private fun handleDeleteAction(position: Int) {
        val task = dataList[position]
        dataSource?.deleteTask(task)
        dataList.removeAt(position)
        adapter?.notifyItemRemoved(position)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val text = data.getStringExtra("text")
            val subtext = data.getStringExtra("subtext")
            val time = data.getStringExtra("time")
            val isChecked = data.getBooleanExtra("isChecked", false)
            val task = Task(text!!, subtext!!)

            val id = dataSource?.addTask(task) ?: -1L
            if (id != -1L) {
                task.id = id
                dataList.add(task)
                adapter?.notifyItemInserted(dataList.size - 1)
            }
        } else if (requestCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val editedText = data.getStringExtra("text")
            val editedSubtext = data.getStringExtra("subtext")
            val editedTime = data.getStringExtra("time")
            val isChecked = data.getBooleanExtra("isChecked", false)
            val position = data.getIntExtra("position", -1)
            val id = data.getIntExtra("id", -1)

            if (position != -1) {
                val task = dataList[position]
                task.text = editedText!!
                task.subtext = editedSubtext!!

                dataSource?.updateTask(id.toLong(), editedText, editedSubtext)
                adapter?.notifyItemChanged(position)
            }
        }
    }

    companion object {
        private const val ADD_REQUEST_CODE = 1 // Request code for adding task
        private const val EDIT_REQUEST_CODE = 2 // Request code for editing task
    }
}
