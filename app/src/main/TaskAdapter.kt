package com.vpk.eduseed
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val context: Context,
    private val taskList: MutableList<Task>,
    private val dataSource: DataSource
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.textViewText.text = task.text
        holder.textViewSubtext.text = task.subtext
        holder.textViewTime.text = task.time
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = task.isChecked

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            Log.d("TaskAdapter", "Checkbox state changed for task ID: ${task.id} to: $isChecked")
            task.isChecked = isChecked
            dataSource.updateTask(task.id, task.text, task.subtext, task.time, task.isChecked)
            notifyItemChanged(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = taskList.size

    fun addTask(task: Task) {
        taskList.add(task)
        notifyItemInserted(taskList.size - 1)
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewText: TextView = itemView.findViewById(R.id.textViewTitle)
        val textViewSubtext: TextView = itemView.findViewById(R.id.textViewSubTitle)
        val textViewTime: TextView = itemView.findViewById(R.id.textViewDate)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkIcon)
    }
}