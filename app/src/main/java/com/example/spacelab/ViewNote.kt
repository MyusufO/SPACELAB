package com.example.spacelab

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ViewNoteActivity : AppCompatActivity(), OnNoteSavedListener {

    private lateinit var noteTextView: TextView
    private lateinit var recyclerViewImages: RecyclerView
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var tasksAdapter: TaskAdapter
    private val imageList = mutableListOf<Uri?>()
    private val taskList = mutableListOf<TaskList>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_note)

        noteTextView = findViewById(R.id.noteTextView)
        recyclerViewImages = findViewById(R.id.recyclerViewImages)
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks)
        val backButton: Button = findViewById(R.id.backButton)

        val title = intent.getStringExtra("title")

        imageAdapter = ImageAdapter(this, imageList)
        recyclerViewImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewImages.adapter = imageAdapter

        tasksAdapter = TaskAdapter(taskList, this, true)
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        recyclerViewTasks.adapter = tasksAdapter

        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users/$userID/notes/$title")

        reference.child("text").get().addOnSuccessListener { dataSnapshot ->
            val noteText = dataSnapshot.value.toString()
            noteTextView.text = noteText
        }.addOnFailureListener {
            println("Error: ${it.message}")
        }

        reference.child("Images").get().addOnSuccessListener { dataSnapshot ->
            for (imageSnapshot in dataSnapshot.children) {
                val imageURL = imageSnapshot.value.toString()
                imageList.add(Uri.parse(imageURL))
            }
            imageAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            println("Error fetching images: ${it.message}")
        }

        // Retrieve and display tasks
        reference.child("tasks").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                taskList.clear()
                for (taskSnapshot in dataSnapshot.children) {
                    val label = taskSnapshot.child("label").value.toString()
                    val datetime = taskSnapshot.child("datetime").value.toString()
                    taskList.add(TaskList(label, datetime))
                }
                tasksAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error fetching tasks: ${error.message}")
            }
        })

        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onNoteSaved() {
        // Handle the onNoteSaved event if needed
    }
}
