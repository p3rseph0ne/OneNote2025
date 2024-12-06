package com.onenote.android

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class NoteEditActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var preferences: Preferences
    private lateinit var db: Database
    private var id = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)

        // Init Preferences
        preferences = Preferences(this)

        // Init FusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        // Find views by ID
        val noteEditTitle = findViewById<EditText>(R.id.noteEditTitle)
        val noteEditMessage = findViewById<EditText>(R.id.noteEditMessage)
        val buttonSave = findViewById<Button>(R.id.buttonSave)
        val buttonLocation = findViewById<Button>(R.id.buttonLocation)

        // Init database
        db = Database(this)
        id = intent.getLongExtra("id", -1)
        if (id >= 0) {
            val note = db.getNote(id)
            noteEditTitle.setText(note?.title)
            noteEditMessage.setText(note?.message)
        }

        // Set OnClickListener
        buttonLocation.setOnClickListener{
            displayLocation()
        }

        buttonSave.setOnClickListener{
            val note = Note(noteEditTitle.editableText.toString(), noteEditMessage.editableText.toString(), id)
            if (id >= 0) {
                db.updateNote(note)
            } else {
                db.insertNote(note)
            }

            Toast.makeText(this, R.string.saved, Toast.LENGTH_LONG).show()

            finish()
        }
    }

    private fun displayLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                Toast.makeText(this, location.toString(), Toast.LENGTH_LONG).show()
            }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            101)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (id >= 0) {
            menuInflater.inflate(R.menu.menu_edit, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.delete) {
            showDeleteDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_message))
            .setPositiveButton(R.string.yes) { _, _ ->
                db.deleteNote(id)
                finish()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
}