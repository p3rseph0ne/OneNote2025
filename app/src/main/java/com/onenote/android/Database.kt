package com.onenote.android

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        // Database properties
        private const val DATABASE_NAME = "onenote"
        private const val DATABASE_TABLE_NAME = "notes"
        private const val DATABASE_VERSION = 1

        // Database table column names
        private const val KEY_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"

        // Create table statement
        private const val CREATE_TABLE = ("""CREATE TABLE $DATABASE_TABLE_NAME(
                $KEY_ID INTEGER PRIMARY KEY,
                $KEY_TITLE TEXT,
                $KEY_MESSAGE TEXT
                )""")

        // Database cursor array
        private val CURSOR_ARRAY = arrayOf(
            KEY_ID,
            KEY_TITLE,
            KEY_MESSAGE
        )

        // Select all statement
        private const val SELECT_ALL = "SELECT * FROM $DATABASE_TABLE_NAME"
    }

    // Insert note into database
    fun insertNote(note: Note): Long {
        val values = noteToContentValues(note)

        return writableDatabase.insert(DATABASE_TABLE_NAME, null, values)
    }

    // Create new ContentValues object note
    private fun noteToContentValues(note: Note): ContentValues {
        val values = ContentValues()

        values.put(KEY_TITLE, note.title)
        values.put(KEY_MESSAGE, note.message)

        return values
    }

    // Get single note from database
    fun getNote(id: Long): Note? {
        val note: Note?
        val cursor = readableDatabase.query(
            DATABASE_TABLE_NAME, CURSOR_ARRAY, "$KEY_ID=?",
            arrayOf(id.toString()), null, null, null, null
        )

        cursor.moveToFirst()
        note = cursorToNote(cursor)
        cursor.close()

        return note
    }

    // Get all notes from database
    fun getAllNotes(): List<Note> {
        val notes = ArrayList<Note>()
        val cursor = readableDatabase.rawQuery(SELECT_ALL, null)

        cursor.moveToFirst().run {
            do {
                cursorToNote(cursor)?.let {
                    notes.add(it)
                }
            } while (cursor.moveToNext())
        }

        return notes
    }

    @SuppressLint("Range")
    private fun cursorToNote(cursor: Cursor):Note? {
        var note: Note? = null
        if (cursor.count == 0) return null

        cursor.run {
            note = Note(
                getString(getColumnIndex(KEY_TITLE)),
                getString(getColumnIndex(KEY_MESSAGE)),
                getLong(getColumnIndex(KEY_ID))
            )
        }

        return note
    }

    // Update single note
    fun updateNote(note: Note): Int {
        return writableDatabase.update(DATABASE_TABLE_NAME,
            noteToContentValues(note),
            "$KEY_ID=?",
            arrayOf(note.id.toString()))
    }

    // Delete single note
    fun deleteNote(id: Long) {
        writableDatabase.delete(
            DATABASE_TABLE_NAME,
            "$KEY_ID=?",
            arrayOf(id.toString())
        )
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
    }
}