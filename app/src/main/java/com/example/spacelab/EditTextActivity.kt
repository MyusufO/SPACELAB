package com.example.spacelab

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditTextActivity : AppCompatActivity() {

    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_text)

        editText = findViewById(R.id.editText)
        val saveButton: Button = findViewById(R.id.saveButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)

        // Get the path from the intent
        val pathAsString = intent.getStringExtra("path")

        // Retrieve the initial text from the database using the path
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference(pathAsString!!)
        reference.child("text").get().addOnSuccessListener { dataSnapshot ->
            val initialText = dataSnapshot.value.toString()
            editText.setText(initialText)
        }.addOnFailureListener {
            // Handle the error
            println("Error: ${it.message}")
        }

        // Save button click listener
        saveButton.setOnClickListener {
            // Save the edited text to the database using the path
            val newText = editText.text.toString()
            reference.child("text").setValue(newText).addOnSuccessListener {
                // Handle the success (optional)
                println("Text updated successfully.")
            }.addOnFailureListener {
                // Handle the error
                println("Error: ${it.message}")
            }

            // Finish the activity
            finish()
        }

        // Cancel button click listener
        cancelButton.setOnClickListener {
            // Finish the activity without saving
            finish()
        }
    }
}
