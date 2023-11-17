package com.example.spacelab

import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotesAdapter(private val notesList: List<Note>) :
    RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notesList[position]
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.noteTitleTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.noteContentTextView)
        val optionsButton: Button = itemView.findViewById(R.id.optionsButton)

        init {
            optionsButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val note = notesList[position]
                    showOptionsPopup(itemView, note)
                }
            }
        }
    }

    private fun showOptionsPopup(view: View, note: Note) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.options_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
            when (item?.itemId) {

                R.id.menu_delete -> {
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
                                        var childExist: Boolean
                                        var userInput =note.title
                                        val reference1 = db.getReference("Users/$userKey")
                                        val child = reference1.child("notes").child(userInput)
                                        child.removeValue()
                                        break

                                    }

                                }
                            }
                        }
                        else {
                            // Handle the error
                            println("Error: ${task.exception?.message}")
                        }
                    }
                    true
                }
                R.id.menu_edit -> {
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
                                        var childExist: Boolean
                                        var userInput = note.title
                                        val reference1 = db.getReference("Users/$userKey")
                                        val child = reference1.child("notes").child(userInput)

                                        // Get the path as a string
                                        val pathAsString = child.toString()

                                        // Create an Intent to start the EditTextActivity
                                        val intent = Intent(view.context, EditTextActivity::class.java)
                                        intent.putExtra("path", "Users/$userKey/notes/$userInput")

                                        view.context.startActivity(intent)

                                        // No need to remove the value here since you are navigating to EditTextActivity
                                        break
                                    }
                                }
                            }
                        } else {
                            // Handle the error
                            println("Error: ${task.exception?.message}")
                        }
                    }


                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }



}
