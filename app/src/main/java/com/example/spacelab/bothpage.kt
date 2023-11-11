package com.example.spacelab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button

class bothpage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
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