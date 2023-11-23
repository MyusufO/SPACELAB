package com.example.spacelab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CreateNote : AppCompatActivity() {

    // Request code for picking an image
    private val PICK_IMAGE_REQUEST = 1

    // UI elements
    private lateinit var mButtonUpload: Button
    private lateinit var mImageView: RecyclerView
    private lateinit var editText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var removeImageButton: Button

    // Firebase references
    private lateinit var mStorageRef: StorageReference
    private lateinit var mDatabaseRef: DatabaseReference

    // List to store image URIs
    private val imageList = mutableListOf<Uri?>()

    // Currently selected image URI
    private var mImageUri: Uri? = null

    // Custom ItemClickListener for RecyclerView
    private lateinit var recyclerItemClickListener: RecyclerItemClickListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve username from the intent extras
        val extras = intent.extras
        val username = extras?.getString("title")

        // Set the layout for this activity
        setContentView(R.layout.activity_create_note)

        // Initialize UI elements
        mButtonUpload = findViewById(R.id.image)
        mImageView = findViewById(R.id.recyclerView)
        editText = findViewById(R.id.editText)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        removeImageButton = findViewById(R.id.removeImageButton)

        // Initialize Firebase references
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads")
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(username!!)

        // Configure RecyclerView
        mImageView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mImageView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL))

        // Set the OnClickListener for removeImageButton
        removeImageButton.setOnClickListener {
            Toast.makeText(this, "Click on an image to remove", Toast.LENGTH_SHORT).show()

            // Initialize and attach custom ItemTouchListener for RecyclerView
            recyclerItemClickListener = RecyclerItemClickListener(this, mImageView,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        removeImage(position)
                    }
                })
            mImageView.addOnItemTouchListener(recyclerItemClickListener)
        }

        // Set OnClickListener for the upload button
        mButtonUpload.setOnClickListener {
            openFileChooser()

            // Remove the custom ItemTouchListener if it's initialized
            if (::recyclerItemClickListener.isInitialized) {
                mImageView.removeOnItemTouchListener(recyclerItemClickListener)
            }
        }

        // Create and set the adapter for the RecyclerView
        val imageAdapter = ImageAdapter(this, imageList)
        mImageView.adapter = imageAdapter

        // Set OnClickListener for the save button
        saveButton.setOnClickListener {
            // Retrieve text input
            val inputText = editText.text.toString()

            // Get the current user ID
            val userID = FirebaseAuth.getInstance().currentUser!!.uid

            // Define the path for storing images in Firebase Storage
            val imagesPath = "Images"

            // Reference to the user's notes in Firebase Database
            val reference: DatabaseReference =
                FirebaseDatabase.getInstance().getReference("Users/$userID/notes/$username")

            // Save text input to the database
            reference.child("text").setValue(inputText)

            // Save images to the database using the path
            val imagesRef = reference.child("Images")

            // Clear existing images and add the new list of image URLs
            imagesRef.removeValue().addOnSuccessListener {
                for (i in imageList.indices) {
                    // Save the image URLs directly in the database
                    imagesRef.child("$i").setValue(imageList[i].toString())
                }

                // Display a toast message indicating that the note is saved
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()

                // Redirect to the main activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

            }.addOnFailureListener {
                // Handle the error
                Toast.makeText(this, "Error clearing existing images: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }


        // Set OnClickListener for the cancel button
        cancelButton.setOnClickListener {
            // Redirect to the main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // Open the file chooser to pick an image
    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Handle the result of picking an image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            mImageUri = data.data
            imageList.add(mImageUri)
            mImageView.adapter?.notifyDataSetChanged()
        }
    }

    // Remove the selected image at the specified position
    private fun removeImage(position: Int) {
        if (position in 0 until imageList.size) {
            imageList.removeAt(position)
            mImageView.adapter?.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "Invalid image position", Toast.LENGTH_SHORT).show()
        }
    }
}
