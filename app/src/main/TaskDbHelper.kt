package com.vpk.eduseed


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class TaskDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "boxes.db"
        const val TABLE_NAME = "box"
        const val COLUMN_TEXT = "text"
        const val COLUMN_SUBTEXT = "subtext"
        const val COLUMN_STATE = "state"
        const val COLUMN_TIME = "time"
        const val COLUMN_ID = BaseColumns._ID

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_TEXT TEXT, $COLUMN_SUBTEXT TEXT, $COLUMN_TIME TEXT, $COLUMN_STATE INTEGER);"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"
    }
}