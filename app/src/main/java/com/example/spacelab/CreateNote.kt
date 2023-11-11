package com.example.spacelab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class CreateNote : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras
        val username = extras?.getString("path")
        Toast.makeText(
            this@CreateNote,
            "OKAY - Input: $username",
            Toast.LENGTH_SHORT
        ).show()
        setContentView(R.layout.activity_create_note)
    }
}