// CreateNote.kt
package com.example.spacelab

import android.annotation.SuppressLint
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

class CreateNote : AppCompatActivity(), OnNoteSavedListener {

    private val PICK_IMAGE_REQUEST = 1

    private lateinit var mButtonUpload: Button
    private lateinit var mImageView: RecyclerView
    private lateinit var editText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var removeImageButton: Button
    private lateinit var checkboxAdapter: TaskAdapter
    private lateinit var checkboxRecyclerView: RecyclerView
    private lateinit var mStorageRef: StorageReference
    private lateinit var mDatabaseRef: DatabaseReference
    private val imageList = mutableListOf<Uri?>()
    private var mImageUri: Uri? = null
    private lateinit var recyclerItemClickListener: RecyclerItemClickListener

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        val username = extras?.getString("title")

        setContentView(R.layout.activity_create_note)

        mButtonUpload = findViewById(R.id.image)
        mImageView = findViewById(R.id.recyclerView)
        editText = findViewById(R.id.editText)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        removeImageButton = findViewById(R.id.removeImageButton)

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads")
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(username!!)
        checkboxAdapter = TaskAdapter(mutableListOf(), this)

        mImageView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mImageView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL))

        checkboxRecyclerView = findViewById(R.id.recyclerViewCheckboxes)
        checkboxRecyclerView.layoutManager = LinearLayoutManager(this)
        checkboxRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        checkboxRecyclerView.adapter = checkboxAdapter

        checkboxAdapter.enableSwipeToDelete(checkboxRecyclerView)

        val addTask: Button = findViewById(R.id.addTask)
        addTask.setOnClickListener {
            checkboxAdapter.addItem(TaskList("Title"))
        }

        recyclerItemClickListener = RecyclerItemClickListener(this, mImageView,
            object : RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    removeImage(position)
                }
            })
        mImageView.addOnItemTouchListener(recyclerItemClickListener)

        mButtonUpload.setOnClickListener {
            openFileChooser()
            if (::recyclerItemClickListener.isInitialized) {
                mImageView.removeOnItemTouchListener(recyclerItemClickListener)
            }
        }

        val imageAdapter = ImageAdapter(this, imageList)
        mImageView.adapter = imageAdapter

        saveButton.setOnClickListener {
            val inputText = editText.text.toString()
            val userID = FirebaseAuth.getInstance().currentUser!!.uid

            for (i in imageList.indices) {
                val userID = FirebaseAuth.getInstance().currentUser!!.uid
                val imagePath = "Images/$userID"
                val imagesReference = mStorageRef.child("$imagePath/$i")

                imagesReference.putFile(imageList[i]!!)
                    .addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                            val imagesReference =FirebaseDatabase.getInstance().getReference("Users/$userID/notes/$username/Images")// Update this line
                            val imageIndexReference = imagesReference.child("$i") // Update this line
                            imageIndexReference.setValue(uri.toString()) // Update this line
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error uploading image: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            val reference: DatabaseReference =
                FirebaseDatabase.getInstance().getReference("Users/$userID/notes/$username")

            reference.child("text").setValue(inputText)

            val tasksRef = reference.child("tasks")
            tasksRef.removeValue().addOnSuccessListener {
                for ((index, checkboxItem) in checkboxAdapter.checkboxList.withIndex()) {
                    tasksRef.child(index.toString()).setValue(checkboxItem)
                }

                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

            }.addOnFailureListener {
                Toast.makeText(this, "Error saving checkboxes: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            mImageUri = data.data
            imageList.add(mImageUri)
            mImageView.adapter?.notifyDataSetChanged()
        }
    }

    private fun removeImage(position: Int) {
        if (position in 0 until imageList.size) {
            imageList.removeAt(position)
            mImageView.adapter?.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "Invalid image position", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNoteSaved() {
        Toast.makeText(this, "Note saved from CheckboxAdapter", Toast.LENGTH_SHORT).show()
    }
}
