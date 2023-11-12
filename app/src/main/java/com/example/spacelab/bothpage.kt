package com.example.spacelab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

class bothpage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        var mAuth = FirebaseAuth.getInstance()

        // Check if the user is already logged in
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val userEmail = currentUser.email
            if (userEmail != null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                val intent = Intent(this, Signup::class.java)
                startActivity(intent)
                finish()
            }

        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bothpage)

        val login: Button = findViewById(R.id.ToLogin)
        val signup: Button = findViewById(R.id.ToSignup)

        login.setOnClickListener {

            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }

        signup.setOnClickListener{

            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }
    }
}