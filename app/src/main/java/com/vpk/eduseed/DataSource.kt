package com.vpk.eduseed

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class DataSource(context: Context) {

    private val dbHelper: TaskDbHelper = TaskDbHelper(context)
    private val database: SQLiteDatabase = dbHelper.writableDatabase

    fun close() {
        dbHelper.close()
    }

    fun addTask(task: Task): Long {
        val values = ContentValues().apply {
            put(TaskDbHelper.COLUMN_TEXT, task.text)
            put(TaskDbHelper.COLUMN_SUBTEXT, task.subtext)

        }
        return database.insert(TaskDbHelper.TABLE_NAME, null, values)
    }

    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val cursor: Cursor = database.query(
            TaskDbHelper.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(TaskDbHelper.COLUMN_ID))
                val text = getString(getColumnIndexOrThrow(TaskDbHelper.COLUMN_TEXT))
                val subtext = getString(getColumnIndexOrThrow(TaskDbHelper.COLUMN_SUBTEXT))
                val time = getString(getColumnIndexOrThrow(TaskDbHelper.COLUMN_TIME))

                tasks.add(Task(id, text, subtext))
            }
            close()
        }
        return tasks
    }

    fun getAllAlarms(): List<Task> {
        val tasks = mutableListOf<Task>()
        val cursor: Cursor = database.query(
            TaskDbHelper.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(TaskDbHelper.COLUMN_ID))
                val text = getString(getColumnIndexOrThrow(TaskDbHelper.COLUMN_TEXT))
                val subtext = getString(getColumnIndexOrThrow(TaskDbHelper.COLUMN_SUBTEXT))
                val time = getString(getColumnIndexOrThrow(TaskDbHelper.COLUMN_TIME))

                tasks.add(Task(id.toLong(), text, subtext))
            }
            close()
        }
        return tasks
    }

    fun insertTask(task: Task): Long {
        val values = ContentValues().apply {
            put(TaskDbHelper.COLUMN_TEXT, task.text)
            put(TaskDbHelper.COLUMN_SUBTEXT, task.subtext)

        }
        return database.insert(TaskDbHelper.TABLE_NAME, null, values)
    }

    fun deleteTask(task: Task) {
        database.delete(
            TaskDbHelper.TABLE_NAME,
            "${TaskDbHelper.COLUMN_ID} = ?",
            arrayOf(task.id.toString())
        )
    }

    fun updateTask(id: Long, text: String, subtext: String) {
        val values = ContentValues().apply {
            put(TaskDbHelper.COLUMN_TEXT, text)
            put(TaskDbHelper.COLUMN_SUBTEXT, subtext)


        }
        val selection = "${TaskDbHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        database.update(TaskDbHelper.TABLE_NAME, values, selection, selectionArgs)
    }
}
