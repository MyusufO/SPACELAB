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

    // Exit button
    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.exit -> {
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(1)
            }

            else -> {
                // Handle other menu item clicks if needed
            }
        }
        return super.onOptionsItemSelected(item)
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val add = findViewById<Button>(R.id.addNotes)
        val currentuser = FirebaseAuth.getInstance().currentUser

        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        notesList = mutableListOf()
        notesAdapter = NotesAdapter(notesList)

        val layoutManager = LinearLayoutManager(this)
        notesRecyclerView.layoutManager = layoutManager
        notesRecyclerView.adapter = notesAdapter

        if (currentuser != null) {
            val mail = currentuser.email

            add.setOnClickListener {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.alertbox)
                dialog.setCancelable(false)

                val okayButton = dialog.findViewById<Button>(R.id.btnOkay)
                val cancelButton = dialog.findViewById<Button>(R.id.btnCancel)
                val editText = dialog.findViewById<EditText>(R.id.editText)

                dialog.show()

                okayButton.setOnClickListener {
                    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
                    val reference: DatabaseReference = db.getReference("Users")

                    reference.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val dataSnapshot = task.result
                            if (dataSnapshot != null) {
                                var childExist = false
                                var userInput = editText.text.toString()

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
                                                } else {
                                                    // Child node doesn't exist

                                                    val intent = Intent(this@MainActivity, CreateNote::class.java)
                                                    intent.putExtra("path","Users/$userKey/notes/$userInput")
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
                        } else {
                            // Handle the error
                            println("Error: ${task.exception?.message}")
                        }
                    }
                    dialog.dismiss()
                }

                cancelButton.setOnClickListener {
                    dialog.dismiss()
                }
            }

            loadNotesFromDatabase(mail)
        }
    }

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
                            val noteContent = noteSnapshot.getValue(String::class.java)

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
