package com.example.spacelab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        val add=findViewById<Button>(R.id.addNotes)

        add.setOnClickListener {

        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}