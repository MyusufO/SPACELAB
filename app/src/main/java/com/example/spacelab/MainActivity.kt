package com.example.spacelab
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(), NoteActionListener {

    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var notesList: MutableList<Note>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentuser = FirebaseAuth.getInstance().currentUser

        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        notesList = mutableListOf()
        notesAdapter = NotesAdapter(notesList, this)

        val layoutManager = LinearLayoutManager(this)
        notesRecyclerView.layoutManager = layoutManager
        notesRecyclerView.adapter = notesAdapter

        if (currentuser != null) {
            val mail = currentuser.email
            loadNotesFromDatabase(mail)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

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

    override fun onDeleteClicked(note: Note) {
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = db.getReference("Users")
        reference.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot != null) {
                    for (snapshot in dataSnapshot.children) {
                        val userKey = snapshot.key
                        val userEmail = snapshot.child("email").getValue()
                        if (userEmail == FirebaseAuth.getInstance().currentUser!!.email) {
                            val reference1 = db.getReference("Users/$userKey")
                            val child = reference1.child("notes").child(note.title)
                            child.removeValue()
                            break
                        }
                    }
                }
            } else {
                println("Error: ${task.exception?.message}")
            }
        }
    }

    override fun onEditClicked(note: Note) {
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = db.getReference("Users")
        reference.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot != null) {
                    for (snapshot in dataSnapshot.children) {
                        val userKey = snapshot.key
                        val userEmail = snapshot.child("email").getValue()
                        if (userEmail == FirebaseAuth.getInstance().currentUser!!.email) {
                            val reference1 = db.getReference("Users/$userKey")
                            val child = reference1.child("notes").child(note.title)
                            val pathAsString = child.toString()
                            val intent = Intent(this@MainActivity, EditTextActivity::class.java)
                            intent.putExtra("path", pathAsString)
                            startActivity(intent)
                            break
                        }
                    }
                }
            } else {
                println("Error: ${task.exception?.message}")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentuser = FirebaseAuth.getInstance().currentUser
        if (currentuser == null) {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
            finish()
        } else {
            val mail: String = currentuser.email!!
            loadNotesFromDatabase(mail)
            val add = findViewById<Button>(R.id.addNotes)
            add.setOnClickListener {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.alertbox)
                dialog.setCancelable(false)
                dialog.show()
                val okayButton = dialog.findViewById<Button>(R.id.btnOkay)
                val cancelButton = dialog.findViewById<Button>(R.id.btnCancel)
                val editText = dialog.findViewById<EditText>(R.id.editText)
                val Tag = dialog.findViewById<EditText>(R.id.Tag)
                val colorSpinner = dialog.findViewById<Spinner>(R.id.colorSpinner)

                val colors = arrayOf(
                    "Red", "Green", "Blue", "Yellow", "Purple",
                    "Orange", "Pink", "Cyan", "Brown", "Gray"
                )
                val adapter =
                    ArrayAdapter(this, android.R.layout.simple_spinner_item, colors)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                colorSpinner.adapter = adapter

                okayButton.setOnClickListener {
                    path(mail, editText, Tag, colorSpinner)
                    dialog.dismiss()
                }

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
                            val noteContent = noteSnapshot.child("text").getValue(String::class.java)
                            val noteColor = noteSnapshot.child("color").getValue(String::class.java)

                            if (noteTitle != null && noteContent != null && noteColor != null) {
                                notesList.add(Note(noteTitle, noteContent, noteColor))
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

    private fun path(mail: String, editText: EditText, Tag: EditText, colorSpinner: Spinner) {
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = db.getReference("Users")
        reference.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot != null) {
                    for (snapshot in dataSnapshot.children) {
                        val userKey = snapshot.key
                        val userEmail = snapshot.child("email").getValue()
                        if (userEmail == mail) {
                            var childExist: Boolean
                            var userInput = editText.text.toString()
                            val tagtext = Tag.text.toString().lowercase()
                            val selectedColor: String = colorSpinner.selectedItem.toString()
                            val reference1 = db.getReference("Users/$userKey")
                            val child = reference1.child("notes").child(userInput)

                            child.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    childExist = dataSnapshot.exists()
                                    if (childExist) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Title Already Taken",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        val intent = Intent(this@MainActivity, CreateNote::class.java)
                                        child.child("tag").setValue(tagtext)
                                        child.child("color").setValue(selectedColor)
                                        intent.putExtra("path", "Users/$userKey/notes/$userInput")
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
                println("Error: ${task.exception?.message}")
            }
        }
    }
}
