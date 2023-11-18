package com.example.spacelab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CreateNote : AppCompatActivity() {

    // Constant for identifying image pick request
    private val PICK_IMAGE_REQUEST = 1

    // UI elements
    private lateinit var mButtonUpload: Button
    private lateinit var mImageView: RecyclerView
    private lateinit var editText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    // Firebase storage and database references
    private lateinit var mStorageRef: StorageReference
    private lateinit var mDatabaseRef: DatabaseReference

    // Adapter for displaying images in RecyclerView
    private lateinit var imageAdapter: ImageAdapter

    // List to store selected image URIs
    private val imageList = mutableListOf<Uri?>()

    // Variable to store the currently selected image URI
    private var mImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve username from intent extras
        val extras = intent.extras
        val username = extras?.getString("path")

        // Set the layout for this activity
        setContentView(R.layout.activity_create_note)

        // Initialize UI elements
        mButtonUpload = findViewById(R.id.Image)
        mImageView = findViewById(R.id.recyclerView)
        editText = findViewById(R.id.editText)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)

        // Initialize Firebase storage and database references
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads")
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(username!!)

        // Initialize RecyclerView and its adapter
        imageAdapter = ImageAdapter(this, imageList)
        mImageView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mImageView.adapter = imageAdapter

        // Set click listener for the "Upload Image" button
        mButtonUpload.setOnClickListener {
            openFileChooser()
        }

        // Set click listener for the "Save" button
        saveButton.setOnClickListener {
            val inputText = editText.text.toString()
            val userKey = extractUserKey(username)
            val imagesPath = "Images"

            // Save text in Firebase Database
            val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference(username)
            reference.child("text").setValue(inputText)

            // Save selected images in Firebase Storage and their download URLs in Firebase Database
            for (i in imageList.indices) {
                val imagePath = "$imagesPath/$i"
                val imagesReference = mStorageRef.child("$imagePath/$userKey")

                // Upload image to Firebase Storage
                imagesReference.putFile(imageList[i]!!)
                    .addOnSuccessListener { taskSnapshot ->
                        // Get the download URL for the uploaded image
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                            // Save the download URL in Firebase Database
                            val imagesReference = mDatabaseRef.child(imagePath)
                            imagesReference.setValue(uri.toString())
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle error uploading image
                        Toast.makeText(this, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            // Display a toast message indicating that the note is saved
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()

            // Redirect to the main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for the "Cancel" button
        cancelButton.setOnClickListener {
            // Redirect to the main activity without saving
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // Function to open the file chooser for selecting images
    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Override function to handle the result from the file chooser
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            // Retrieve the selected image URI
            mImageUri = data.data

            // Update the RecyclerView with the selected image
            imageList.add(mImageUri)
            imageAdapter.notifyDataSetChanged()
        }
    }

    // Function to extract the user key from the provided string
    fun extractUserKey(inputString: String): String? {
        val regex = Regex("""Users/([^/]+)/notes/\w+""")
        val matchResult = regex.find(inputString)
        return matchResult?.groups?.get(1)?.value
    }
}
