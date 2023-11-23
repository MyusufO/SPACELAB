package com.example.spacelab
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
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
        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        notesList = mutableListOf()
        notesAdapter = NotesAdapter(notesList, this)
        val layoutManager = LinearLayoutManager(this)
        notesRecyclerView.layoutManager = layoutManager
        notesRecyclerView.adapter = notesAdapter
        loadNotesFromDatabase()

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
            R.id.SortTag -> {
                sortNotesByTag()
            }
            R.id.SortColor -> {
                sortNotesByColor()
            }
            else -> {
                // Handle other menu item clicks if needed
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onDeleteClicked(note: Note) {
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val userID=FirebaseAuth.getInstance().currentUser!!.uid
        val reference: DatabaseReference = db.getReference("Users").child(userID).child("notes").child(note.title)
        reference.removeValue()
    }

    override fun onEditClicked(note: Note, view: View) {
        val intent = Intent(this@MainActivity, EditTextActivity::class.java)
        intent.putExtra("title", note.title)
        startActivity(intent)
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
            loadNotesFromDatabase()
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
            loadNotesFromDatabase()
        }
    }

    private fun loadNotesFromDatabase() {
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = db.getReference("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("notes")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                notesList.clear()
                for (snapshot in dataSnapshot.children) {
                    val noteTitle = snapshot.key
                    val noteContent = snapshot.child("text").getValue(String::class.java)
                    val noteColor = snapshot.child("color").getValue(String::class.java)
                    if (noteTitle != null && noteContent != null && noteColor != null) {
                        notesList.add(Note(noteTitle, noteContent, noteColor))
                    }
                }
                notesAdapter.notifyDataSetChanged()


            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error: ${databaseError.message}")
            }
        })
    }

    private fun path(mail: String, editText: EditText, Tag: EditText, colorSpinner: Spinner) {
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = db.getReference("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("notes")
        reference.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var childExist: Boolean
                var userInput = editText.text.toString()
                val tagtext = Tag.text.toString().lowercase()
                val selectedColor: String = colorSpinner.selectedItem.toString()
                val child = reference.child(userInput)
                child.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        childExist = dataSnapshot.exists()
                        if (childExist) {
                            Toast.makeText(
                                this@MainActivity,
                                "Title Already Taken",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else {
                            val intent = Intent(this@MainActivity, CreateNote::class.java)
                            child.child("tag").setValue(tagtext)
                            child.child("color").setValue(selectedColor)
                            intent.putExtra("title",userInput)
                            startActivity(intent)
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        println("Error: ${databaseError.message}")
                    }
                })



            }
            else {
                println("Error: ${task.exception?.message}")
            }
        }
    }
    private fun sortNotesByTag() {
        notesList.sortBy { it.tag }
        notesAdapter.notifyDataSetChanged()
    }

    // Sort notes by color
    private fun sortNotesByColor() {
        notesList.sortBy { it.color }
        notesAdapter.notifyDataSetChanged()
    }
}
