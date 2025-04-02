package com.vpk.eduseed
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UpcomingClassesAdapter(private var enrollmentList: List<EnrollmentData>) :
    RecyclerView.Adapter<UpcomingClassesAdapter.UpcomingClassViewHolder>() {

    inner class UpcomingClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStudentName: TextView = itemView.findViewById(R.id.studentName)
        val tvCourseName: TextView = itemView.findViewById(R.id.className)
        val tvClassTime: TextView = itemView.findViewById(R.id.classTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingClassViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_layout, parent, false)
        return UpcomingClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: UpcomingClassViewHolder, position: Int) {
        val enrollment = enrollmentList[position]
        holder.tvStudentName.text = enrollment.studentName
        holder.tvCourseName.text = enrollment.courseName
        holder.tvClassTime.text = enrollment.classTime
    }

    override fun getItemCount(): Int = enrollmentList.size

    fun updateList(newList: List<EnrollmentData>) {
        enrollmentList = newList
        notifyDataSetChanged()
    }
}
