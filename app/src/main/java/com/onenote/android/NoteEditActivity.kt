package com.onenote.android

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.FileOutputStream

class NoteEditActivity:BaseActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var preferences: Preferences
    private lateinit var db: Database
    private var id = -1L
    private var currentPhotoPath: String = ""
    private lateinit var noteImage: ImageView

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
        val noteLocation = findViewById<TextView>(R.id.location)
        val buttonSave = findViewById<Button>(R.id.buttonSave)
        val buttonLocation = findViewById<Button>(R.id.buttonLocation)
        val buttonUpload = findViewById<Button>(R.id.buttonUpload)
        noteImage = findViewById(R.id.noteImage)

        // Init database
        db = Database(this)
        id = intent.getLongExtra("id", -1)
        if (id >= 0) {
            val note = db.getNote(id)
            noteEditTitle.setText(note?.title)
            noteEditMessage.setText(note?.message)
            noteLocation.text = note?.location
            if (!note?.imagePath.isNullOrEmpty()) {
                currentPhotoPath = note?.imagePath ?: ""
                loadImage()
            }
        }

        buttonUpload.setOnClickListener {
            selectImage()
        }

        // Set OnClickListener
        buttonLocation.setOnClickListener {
            displayLocation()
        }

        buttonSave.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                saveNoteWithLocation()
            }
        }
    }

    private fun selectImage() {
        val options = arrayOf<CharSequence>("Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Photo")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Choose from Gallery" -> {
                    Log.d("PermissionDebug", "Requesting Gallery permission")
                    requestGalleryPermission()
                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun requestGalleryPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {

            Log.d("PermissionDebug", "check gallery permissions ")

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), GALLERY_PERMISSION_REQUEST_CODE)

        } else {

            Log.d("PermissionDebug", "launch gallery method call ")
            launchGallery()
        }
    }


    private fun launchGallery() {
        Log.d("PermissionDebug", "launch gallery")
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY_PICK)
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = System.currentTimeMillis()
        val storageDir: File? = getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun loadImage() {
        if (currentPhotoPath.isNotEmpty()) {
            noteImage.setImageURI(Uri.fromFile(File(currentPhotoPath)))
            noteImage.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    loadImage()
                }
                REQUEST_GALLERY_PICK -> {
                    data?.data?.let { uri ->
                        // Copy the file to our app's private storage
                        val inputStream = contentResolver.openInputStream(uri)
                        val outputFile = createImageFile()
                        val outputStream = FileOutputStream(outputFile)
                        inputStream?.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }
                        loadImage()
                    }
                }
            }
        }
    }

    private fun saveNoteWithLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            saveNote("No Location Permission")
            return
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    val locationStr = if (location != null) {
                        "Lat: ${location.latitude}, Long: ${location.longitude}"
                    } else {
                        "No Location stored"
                    }
                    saveNote(locationStr)
                }
                .addOnFailureListener { e ->
                    saveNote("Location unavailable: ${e.message}")
                }
        } catch (e: SecurityException) {
            saveNote("Location permission error: ${e.message}")
        }
    }

    private fun saveNote(locationStr: String) {
        val noteEditTitle = findViewById<EditText>(R.id.noteEditTitle)
        val noteEditMessage = findViewById<EditText>(R.id.noteEditMessage)

        val note = Note(
            noteEditTitle.editableText.toString(),
            noteEditMessage.editableText.toString(),
            id,
            locationStr,
            currentPhotoPath
        )

        if (id >= 0) {
            db.updateNote(note)
        } else {
            db.insertNote(note)
        }

        vibrate()
        MediaPlayer.create(this, R.raw.beep).start()
        Toast.makeText(this, R.string.saved, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun displayLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_DISPLAY_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val locationText = "Lat: ${location.latitude}, Long: ${location.longitude}"
                    Toast.makeText(this, locationText, Toast.LENGTH_LONG).show()
                    findViewById<TextView>(R.id.location).text = locationText
                } else {
                    Toast.makeText(this, "Location not available", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    saveNoteWithLocation()
                } else {
                    saveNote("No Location Permission")
                }
            }
            LOCATION_DISPLAY_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    displayLocation()
                }
            }
            GALLERY_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    launchGallery()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Only show delete menu for existing notes
        if (id >= 0) {
            menuInflater.inflate(R.menu.menu_edit, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.delete_confirmation_title)
                    .setMessage(R.string.delete_confirmation_message)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        if (id >= 0) {
                            db.deleteNote(id)
                            // Delete associated image file if it exists
                            if (currentPhotoPath.isNotEmpty()) {
                                try {
                                    File(currentPhotoPath).delete()
                                } catch (e: Exception) {
                                    Log.e("NoteEditActivity", "Error deleting image file", e)
                                }
                            }
                            Toast.makeText(this, R.string.note_deleted, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val LOCATION_DISPLAY_PERMISSION_REQUEST_CODE = 1002
        private const val GALLERY_PERMISSION_REQUEST_CODE = 1004
        private const val REQUEST_IMAGE_CAPTURE = 1005
        private const val REQUEST_GALLERY_PICK = 1006
    }
}