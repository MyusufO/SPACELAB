package com.example.spacelab

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class EditTextActivity : AppCompatActivity(), OnNoteSavedListener {

    private lateinit var editText: EditText
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var recyclerViewImages: RecyclerView
    private lateinit var tasksAdapter: TaskAdapter
    private lateinit var imageAdapter: ImageAdapter
    private val taskList = mutableListOf<TaskList>()
    private val imageList = mutableListOf<Uri?>()

    // Activity result contract for image selection
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Handle the result of image selection
            uri?.let {
                imageList.add(it)
                imageAdapter.notifyDataSetChanged()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_text)

        editText = findViewById(R.id.editText)
        recyclerViewTasks = findViewById(R.id.recyclerViewCheckboxes)
        recyclerViewImages = findViewById(R.id.recyclerView)
        val saveButton: Button = findViewById(R.id.saveButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)
        val addImageButton: Button = findViewById(R.id.image)
        val removeImageButton: Button = findViewById(R.id.removeImageButton)
        val addTaskButton: Button = findViewById(R.id.addTask)

        val title = intent.getStringExtra("title")

        // Initialize RecyclerView and its adapter for tasks
        tasksAdapter = TaskAdapter(taskList, this)
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        recyclerViewTasks.adapter = tasksAdapter

        // Initialize RecyclerView and its adapter for images
        imageAdapter = ImageAdapter(this, imageList)
        recyclerViewImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewImages.adapter = imageAdapter

        // Retrieve and display tasks from the database using the path
        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users/$userID/notes/$title")

        reference.child("tasks").get().addOnSuccessListener { dataSnapshot ->
            taskList.clear()
            for (taskSnapshot in dataSnapshot.children) {
                val label = taskSnapshot.child("label").value.toString()
                val datetime = taskSnapshot.child("datetime").value.toString()
                taskList.add(TaskList(label, datetime))
            }
            tasksAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            // Handle the error
            println("Error fetching tasks: ${it.message}")
        }

        // Retrieve and display images from the database using the path
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

        // Swipe-to-delete functionality for tasks
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                tasksAdapter.removeItem(position)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerViewTasks)

        // Add Image button click listener
        addImageButton.setOnClickListener {
            // Open the device's gallery to select an image
            getContent.launch("image/*")
        }

        // Add Task button click listener
        addTaskButton.setOnClickListener {
            // Open a dialog or another activity to input details for a new task
            showAddTaskDialog()
        }

        // Save button click listener
        saveButton.setOnClickListener {
            // Save tasks to the database using the path
            val tasksRef = reference.child("tasks")
            tasksRef.removeValue().addOnSuccessListener {
                for ((index, taskItem) in tasksAdapter.checkboxList.withIndex()) {
                    tasksRef.child(index.toString()).setValue(taskItem)
                }

                // Handle the success (optional)
                println("Tasks updated successfully.")
            }.addOnFailureListener {
                // Handle the error
                println("Error: ${it.message}")
            }

            // Save images to the database using the path
            val imagesRef = reference.child("Images")
            imagesRef.setValue(imageList.map { it.toString() }).addOnSuccessListener {
                // Handle the success (optional)
                println("Images updated successfully.")
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

    // Implement the onNoteSaved method from OnNoteSavedListener
    override fun onNoteSaved() {
        // Handle the onNoteSaved event if needed
    }

    private fun showAddTaskDialog() {
        // You can implement a custom dialog or start a new activity for adding a new task
        // For simplicity, I'll just add a default task to the list
        val defaultLabel = "New Task"
        val defaultDatetime = "Select Date and Time"
        val newTask = TaskList(defaultLabel, defaultDatetime)
        tasksAdapter.addItem(newTask)
    }
}
