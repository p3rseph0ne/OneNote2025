package com.onenote.android

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class NoteListActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private lateinit var listView: ListView
    private lateinit var adapter: NoteAdapter
    private lateinit var db: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_list)

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))

        // Find views by ID
        listView = findViewById(R.id.listView)

        db = Database(this)
        adapter = NoteAdapter(this, db.getAllNotes())

        // Set adapter
        listView.adapter = adapter
        listView.onItemClickListener = this
    }

    override fun onResume() {
        super.onResume()

        // Reload notes
        adapter.notes = db.getAllNotes()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add) {

            // Open NoteEditActivity
            val intent = Intent(this, NoteEditActivity::class.java)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, id: Long) {
        val intent = Intent(this, NoteEditActivity::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }
}