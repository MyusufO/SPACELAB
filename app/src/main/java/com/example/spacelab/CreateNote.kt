package com.example.spacelab

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateNote : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras

        val username = extras?.getString("path")
        setContentView(R.layout.activity_create_note)

        // Reference the EditText and Buttons by their IDs
        editText = findViewById(R.id.editText)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)

        saveButton.setOnClickListener {
            val inputText = editText.text.toString()
            val db: FirebaseDatabase = FirebaseDatabase.getInstance()
            if(username!=null) {
                val reference: DatabaseReference = db.getReference(username)
                reference.setValue(inputText)
            }
        }

        cancelButton.setOnClickListener {
            val intent=Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
    }
}
