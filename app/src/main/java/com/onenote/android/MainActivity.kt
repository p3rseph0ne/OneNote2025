package com.onenote.android

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find views by IDs
        val buttonLogin = findViewById<Button>(R.id.login)
        val imageViewIcon = findViewById<ImageView>(R.id.icon)

        // Animate Icon
        val animationRotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        imageViewIcon.startAnimation(animationRotation)

        // Set OnClickListener
        buttonLogin.setOnClickListener{
            // Show Signed-In Toast
            Toast.makeText(this, R.string.signed_in, Toast.LENGTH_LONG).show()

            // Create Intent and open NoteListActivity
            val intent = Intent(this, NoteListActivity::class.java)
            startActivity(intent)

            // Finish MainActivity
            finish()
        }
    }
}