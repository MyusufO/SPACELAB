package com.example.spacelab

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

class LoginPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)
        val emaile = findViewById<EditText>(R.id.Login_mail)
        val passworde = findViewById<EditText>(R.id.LoginPassword)
        val Login = findViewById<Button>(R.id.Loginconfirm)
        val auth = FirebaseAuth.getInstance()

        Login.setOnClickListener {
            var email: String = emaile.text.toString()
            var password: String = passworde.text.toString()
            if(email.isEmpty()){
                Toast.makeText(baseContext, "Enter the mail first", Toast.LENGTH_SHORT).show()
            }
            else if(password.isEmpty()){
                Toast.makeText(baseContext, "Enter the password first", Toast.LENGTH_SHORT).show()
            }
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()

                    }
                }
        }
    }
}