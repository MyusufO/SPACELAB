package com.example.spacelab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginPage : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        mAuth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleSignInButton = findViewById<Button>(R.id.Loginconfirm)
        googleSignInButton.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
           // val signInIntent = googleSignInClient.signInIntent
            //startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        val emailEditText = findViewById<EditText>(R.id.LoginEmail)
        val passwordEditText = findViewById<EditText>(R.id.LoginPassword)
        val emailPasswordLoginButton = findViewById<Button>(R.id.LoginConfirm)

        emailPasswordLoginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Check if email and password are not empty
            if (email.isNotEmpty() && password.isNotEmpty()) {
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish() // Optional, to close the login activity if you don't want the user to go back to it

                            // You can also save user data to Firebase Realtime Database or Firestore
                        } else {
                            // Handle login failure here
                            Log.e(TAG, "Email/password login failed", task.exception)
                            // Display an error message or handle the failure in some way
                            Toast.makeText(this, "Email/Password login failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // Handle empty email or password fields here (e.g., display an error message).
                Toast.makeText(this, "Email and password are required.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException) {
                // Handle Google sign-in failure here
                Log.w(TAG, "Google sign-in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in with Google successful
                    // You can redirect to the main activity here
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Optional, to close the login activity if you don't want the user to go back to it

                    // You can also save user data to Firebase Realtime Database or Firestore
                } else {
                    // Handle Google sign-in failure here
                    Log.e(TAG, "Google sign-in failed", task.exception)
                    // Display an error message or handle the failure in some way
                    Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "LoginPage"
    }
}
