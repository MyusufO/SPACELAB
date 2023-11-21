package  com.example.spacelab

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EditTextActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private val imageList = mutableListOf<Uri?>()

    // Activity result contract for image selection
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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
        recyclerView = findViewById(R.id.recyclerView)
        val saveButton: Button = findViewById(R.id.saveButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)
        val addImageButton: Button = findViewById(R.id.addImageButton)
        val removeImageButton: Button = findViewById(R.id.removeImageButton)

        val title = intent.getStringExtra("title")

        // Initialize RecyclerView and its adapter
        imageAdapter = ImageAdapter(this, imageList)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = imageAdapter

        // Retrieve the initial text and multiple images from the database using the path
        val userID=FirebaseAuth.getInstance().currentUser!!.uid
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users/$userID/notes/$title")
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

        // Add Image button click listener
        addImageButton.setOnClickListener {
            // Open the device's gallery to select an image
            getContent.launch("image/*")
        }

        // Remove Image button click listener
        removeImageButton.setOnClickListener {
            // Display a message to guide the user on how to remove an image
            showToastForRemoveImage()

            // Initialize the recycler item click listener for removal
            val recyclerItemClickListener = RecyclerItemClickListener(this, recyclerView,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: android.view.View, position: Int) {
                        removeImage(position)
                    }
                })

            // Add the item click listener
            recyclerView.addOnItemTouchListener(recyclerItemClickListener)
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

    private fun showToastForRemoveImage() {
        // Display a message to guide the user on how to remove an image
        Toast.makeText(this, "Click on an image to remove", Toast.LENGTH_SHORT).show()
    }

    private fun removeImage(position: Int) {
        // Remove the image at the specified position
        if (position in 0 until imageList.size) {
            imageList.removeAt(position)
            imageAdapter.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "Invalid image position", Toast.LENGTH_SHORT).show()
        }
    }
}
