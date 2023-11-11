package com.example.spacelab

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val add = findViewById<Button>(R.id.addNotes)
        val currentuser = FirebaseAuth.getInstance().currentUser

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
                                    val userEmail = snapshot.getValue()

                                    if (userEmail == mail) {
                                        val reference1 = db.getReference("Users/$userKey")
                                        val child = reference1.child(userInput)

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
                                                    intent.putExtra("path","Users/$userKey/$userInput")
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
        }
    }
}
