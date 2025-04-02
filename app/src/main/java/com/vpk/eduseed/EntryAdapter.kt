package com.vpk.eduseed

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class EntryAdapter(private val entries: MutableList<Entry>) : RecyclerView.Adapter<EntryAdapter.EntryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.student_list, parent, false)
        return EntryViewHolder(view)
    }

    fun addEntry(entry: Entry) {
        if (!entries.contains(entry)) {
            entries.add(entry)
            notifyItemInserted(entries.size - 1)
        }
    }

    fun updateEntries(newEntries: List<Entry>) {
        entries.clear()
        entries.addAll(newEntries)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val entry = entries[position]
        holder.studentName.text = entry.student
        holder.courseName.text = entry.course

        holder.moreVertLayout.setOnClickListener {
            val popupMenu = PopupMenu(it.context, it)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_entry -> {
                        val intent = Intent(it.context, EntryActivity::class.java)
                        intent.putExtra("student_name", entry.student)
                        it.context.startActivity(intent)
                        true
                    }
                    R.id.menu_edit -> {
                        showEditDialog(holder, position)
                        true
                    }
                    R.id.menu_delete -> {
                        deleteEntry(holder, position)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }
    private fun deleteEntry(holder: EntryViewHolder, position: Int) {
        val entry = entries.getOrNull(position) ?: return  // Prevent crash if index is invalid
        val context = holder.itemView.context

        FirebaseDatabase.getInstance().getReference("Entry")
            .orderByChild("student").equalTo(entry.student)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.ref.removeValue().addOnSuccessListener {
                            if (position < entries.size) {  // Ensure position is valid
                                entries.removeAt(position)
                                notifyItemRemoved(position)
                                notifyItemRangeChanged(position, entries.size)

                                if (entries.isEmpty()) {
                                    Toast.makeText(context, "No entries left", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to delete entry", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showEditDialog(holder: EntryViewHolder, position: Int) {
        val entry = entries[position]
        val context = holder.itemView.context
        val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_sample_folder, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        dialog.show()

        val edtStudent = dialogView.findViewById<EditText>(R.id.student)
        val edtCourse = dialogView.findViewById<EditText>(R.id.course)
        edtStudent.setText(entry.student)
        edtCourse.setText(entry.course)

        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSubmit.setOnClickListener {
            val newStudentName = edtStudent.text.toString().trim()
            val newCourseName = edtCourse.text.toString().trim()

            if (newStudentName.isNotEmpty() && newCourseName.isNotEmpty()) {
                FirebaseDatabase.getInstance().getReference("Entry")
                    .orderByChild("student").equalTo(entry.student)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (child in snapshot.children) {
                                child.ref.child("student").setValue(newStudentName)
                                child.ref.child("course").setValue(newCourseName)
                                    .addOnSuccessListener {
                                        entry.student = newStudentName
                                        entry.course = newCourseName
                                        notifyItemChanged(position)
                                        dialog.dismiss()
                                        Toast.makeText(context, "Entry updated", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Failed to update entry", Toast.LENGTH_SHORT).show()
                        }
                    })
            } else {
                Toast.makeText(context, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = entries.size

    class EntryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val studentName: TextView = view.findViewById(R.id.student_name)
        val courseName: TextView = view.findViewById(R.id.course_name)
        val moreVertLayout: LinearLayout = view.findViewById(R.id.moreVertLayout)
    }
}

