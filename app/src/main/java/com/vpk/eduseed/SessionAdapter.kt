package com.vpk.eduseed
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class SessionAdapter(private val sessionList: List<Session>) :
    RecyclerView.Adapter<SessionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val studentName: TextView = view.findViewById(R.id.textViewSessionTitle)
        val courseName: TextView = view.findViewById(R.id.textViewTitle)
        val sessionCount: TextView = view.findViewById(R.id.textViewSubTitle)
        val cardView: CardView = view.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.session_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessionList[position]
        holder.studentName.text = session.studentName
        holder.courseName.text = session.courseName
        holder.sessionCount.text = "Classes: ${session.classCount}"
    }

    override fun getItemCount() = sessionList.size
}

