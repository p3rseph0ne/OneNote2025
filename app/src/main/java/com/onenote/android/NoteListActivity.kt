package com.onenote.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class NoteListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_list)

        setSupportActionBar(findViewById(R.id.toolbar))
    }
}