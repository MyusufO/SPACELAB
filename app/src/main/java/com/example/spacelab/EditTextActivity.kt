package com.example.spacelab

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class EditTextActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private val imageList = mutableListOf<Uri?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_text)

        editText = findViewById(R.id.editText)
        recyclerView = findViewById(R.id.recyclerView)
        val saveButton: Button = findViewById(R.id.saveButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)

        val pathAsString = intent.getStringExtra("path")

        // Initialize RecyclerView and its adapter
        imageAdapter = ImageAdapter(this, imageList)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = imageAdapter

        // Retrieve the initial text and multiple images from the database using the path
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference(pathAsString!!)
        reference.child("text").get().addOnSuccessListener { dataSnapshot ->
            val initialText = dataSnapshot.value.toString()
            editText.setText(initialText)
        }.addOnFailureListener {
            // Handle the error
            println("Error: ${it.message}")
        }

        // Retrieve and display multiple images from the database using the path
        reference.child("Images").get().addOnSuccessListener { dataSnapshot ->
            for (imageSnapshot in dataSnapshot.children) {
                val imageURL = imageSnapshot.value.toString()
                imageList.add(Uri.parse(imageURL))
            }
            imageAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            // Handle the error
            println("Error fetching images: ${it.message}")
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
