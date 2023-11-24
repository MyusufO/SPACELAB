package  com.example.spacelab

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

// Main activity for creating notes
class CreateNote : AppCompatActivity(), CheckboxAdapter.OnNoteSavedListener {

    // Constants
    private val PICK_IMAGE_REQUEST = 1

    // UI elements
    private lateinit var mButtonUpload: Button
    private lateinit var mImageView: RecyclerView
    private lateinit var editText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var removeImageButton: Button
    private lateinit var checkboxAdapter: CheckboxAdapter
    private lateinit var checkboxRecyclerView: RecyclerView
    private lateinit var mStorageRef: StorageReference
    private lateinit var mDatabaseRef: DatabaseReference
    private val imageList = mutableListOf<Uri?>()
    private var mImageUri: Uri? = null

    // Custom item click listener for RecyclerView
    private lateinit var recyclerItemClickListener: RecyclerItemClickListener

    // Called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get username from intent extras
        val extras = intent.extras
        val username = extras?.getString("title")

        // Set the layout for the activity
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

        // Initialize CheckboxAdapter for checkboxes
        checkboxAdapter = CheckboxAdapter(mutableListOf(), this)

        // Configure the image RecyclerView
        mImageView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mImageView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL))

        // Configure the checkbox RecyclerView
        checkboxRecyclerView = findViewById(R.id.recyclerViewCheckboxes)
        checkboxRecyclerView.layoutManager = LinearLayoutManager(this)
        checkboxRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        checkboxRecyclerView.adapter = checkboxAdapter

        // Enable swipe-to-delete for checkboxRecyclerView
        checkboxAdapter.enableSwipeToDelete(checkboxRecyclerView)

        // Set click listener for adding checkboxes
        val addCheckboxButton: Button = findViewById(R.id.addCheckboxButton)
        addCheckboxButton.setOnClickListener {
            val checkboxLabel = editText.text.toString()
            checkboxAdapter.addItem(CheckboxItem(checkboxLabel))
            editText.text.clear()
        }

        // Set item click listener for image RecyclerView
        recyclerItemClickListener = RecyclerItemClickListener(this, mImageView,
            object : RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    removeImage(position)
                }
            })
        mImageView.addOnItemTouchListener(recyclerItemClickListener)

        // Set click listener for image upload button
        mButtonUpload.setOnClickListener {
            openFileChooser()
            if (::recyclerItemClickListener.isInitialized) {
                mImageView.removeOnItemTouchListener(recyclerItemClickListener)
            }
        }

        // Initialize and set adapter for image RecyclerView
        val imageAdapter = ImageAdapter(this, imageList)
        mImageView.adapter = imageAdapter

        // Set click listener for save button
        saveButton.setOnClickListener {
            // Get input text and user ID
            val inputText = editText.text.toString()
            val userID = FirebaseAuth.getInstance().currentUser!!.uid

            // Firebase paths
            val reference: DatabaseReference =
                FirebaseDatabase.getInstance().getReference("Users/$userID/notes/$username")

            // Save text input to Firebase
            reference.child("text").setValue(inputText)

            // Save checkboxes to Firebase under "Tasks"
            val tasksRef = reference.child("tasks")
            tasksRef.removeValue().addOnSuccessListener {
                for ((index, checkboxItem) in checkboxAdapter.checkboxList.withIndex()) {
                    tasksRef.child(index.toString()).setValue(checkboxItem)
                }

                // Display success message and navigate to the main activity
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

            }.addOnFailureListener {
                // Display error message if checkbox saving fails
                Toast.makeText(this, "Error saving checkboxes: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Set click listener for cancel button
        cancelButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // Open file chooser for image selection
    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Handle result from file chooser
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            mImageUri = data.data
            imageList.add(mImageUri)
            mImageView.adapter?.notifyDataSetChanged()
        }
    }

    // Remove image at the specified position
    private fun removeImage(position: Int) {
        if (position in 0 until imageList.size) {
            imageList.removeAt(position)
            mImageView.adapter?.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "Invalid image position", Toast.LENGTH_SHORT).show()
        }
    }

    // Callback method from CheckboxAdapter when a note is saved
    override fun onNoteSaved() {
        // You can add any additional logic you want when a note is saved
        // For example, you might want to perform some action or show a message
        // Here, we'll just display a Toast message
        Toast.makeText(this, "Note saved from CheckboxAdapter", Toast.LENGTH_SHORT).show()
    }
}
