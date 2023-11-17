package com.example.spacelab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class CreateNote : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var mButtonUpload: Button
    private var mImageUri: Uri? = null
    private lateinit var editText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var  mImageView: ImageView
    private lateinit var mStorageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras

        val username = extras?.getString("path")
        setContentView(R.layout.activity_create_note)

        // Reference the EditText and Buttons by their IDs
        mButtonUpload = findViewById(R.id.Image);
        mImageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editText)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)


        //Choosing image
        mButtonUpload.setOnClickListener{
            openFileChooser()
        }

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");

        saveButton.setOnClickListener {
            val inputText = editText.text.toString()
            val db: FirebaseDatabase = FirebaseDatabase.getInstance()
            if(username!=null) {
                val reference: DatabaseReference = db.getReference(username)
                reference.setValue(inputText)
            }
            Toast.makeText(this, "Note saved ", Toast.LENGTH_SHORT).show()
            val intent=Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        cancelButton.setOnClickListener {
            val intent=Intent(this,MainActivity::class.java)
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
            mImageView.setImageURI(mImageUri)
        }
    }

}
