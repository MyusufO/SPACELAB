package com.example.spacelab

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.toLowerCase
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var notesList: MutableList<Note>

    //Opens the toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //Assigning tasks to carry out to the items inside toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.out -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this@MainActivity, LoginPage::class.java))
                finish()
            }
            else -> {
                // Handle other menu item clicks if needed
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements

        val currentuser = FirebaseAuth.getInstance().currentUser


        // Initialize RecyclerView and associated components
        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        notesList = mutableListOf()
        notesAdapter = NotesAdapter(notesList)

        val layoutManager = LinearLayoutManager(this)
        notesRecyclerView.layoutManager = layoutManager
        notesRecyclerView.adapter = notesAdapter

        // Check if the user is logged in
        if (currentuser != null) {
            val mail = currentuser.email



            // Load existing notes from the database
            loadNotesFromDatabase(mail)
        }
    }
    override fun onStart() {
        super.onStart()

        val currentuser = FirebaseAuth.getInstance().currentUser

        if (currentuser == null) {
            // If the user is not logged in, redirect to the login page
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
            finish()
        }
        else{

                val mail = currentuser.email



                // Load existing notes from the database
                loadNotesFromDatabase(mail)


            // Add notes button functionality
            val add = findViewById<Button>(R.id.addNotes)
            add.setOnClickListener {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.alertbox)
                dialog.setCancelable(false)

                val okayButton = dialog.findViewById<Button>(R.id.btnOkay)
                val cancelButton = dialog.findViewById<Button>(R.id.btnCancel)
                val editText = dialog.findViewById<EditText>(R.id.editText)
                val Tag=dialog.findViewById<EditText>(R.id.Tag)
                dialog.show()

                // Okay button click listener
                okayButton.setOnClickListener {
                    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
                    val reference: DatabaseReference = db.getReference("Users")
                    reference.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val dataSnapshot = task.result
                            if (dataSnapshot != null) {

                                var childExist: Boolean
                                var userInput = editText.text.toString()
                                var tagtext= Tag.text.toString().lowercase()
                                for (snapshot in dataSnapshot.children) {
                                    val userKey = snapshot.key
                                    val userEmail = snapshot.child("email").getValue()
                                    if (userEmail == mail) {
                                        val reference1 = db.getReference("Users/$userKey")
                                        val child = reference1.child("notes").child(userInput)

                                        child.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                childExist = dataSnapshot.exists()
                                                if (childExist) {
                                                    // Child node exists
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Title Already Taken",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                else {
                                                    // Child node doesn't exist

                                                    val intent = Intent(this@MainActivity, CreateNote::class.java)
                                                    child.child("tag").setValue(tagtext)
                                                    intent.putExtra("path","Users/$userKey/notes/$userInput/text")
                                                    startActivity(intent)
                                                }
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {
                                                println("Error: ${databaseError.message}")
                                            }
                                        })
                                    }
                                }
                            }
                        }
                        else {
                            // Handle the error
                            println("Error: ${task.exception?.message}")
                        }
                    }
                    dialog.dismiss()
                }

                // Cancel button click listener
                cancelButton.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }
    }
    override fun onPause() {
        super.onPause()
        notesList.clear()
    }

    override fun onResume() {
        super.onResume()

        val currentuser = FirebaseAuth.getInstance().currentUser
        if (currentuser != null) {
            val mail = currentuser.email



            // Load existing notes from the database
            loadNotesFromDatabase(mail)
        }
    }


    // Function to display notes on Screen
    private fun loadNotesFromDatabase(userEmail: String?) {
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = db.getReference("Users")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                notesList.clear()

                for (snapshot in dataSnapshot.children) {
                    val userEmailFromDB = snapshot.child("email").getValue(String::class.java)

                    if (userEmail == userEmailFromDB) {
                        for (noteSnapshot in snapshot.child("notes").children) {
                            val noteTitle = noteSnapshot.key
                            val noteContent = noteSnapshot.child("text").getValue(String::class.java)

                            if (noteTitle != null && noteContent != null) {
                                notesList.add(Note(noteTitle, noteContent))
                            }
                        }
                        notesAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error: ${databaseError.message}")
            }
        })
    }
}
