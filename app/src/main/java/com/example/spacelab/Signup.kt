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

class Signup : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)


        mAuth = FirebaseAuth.getInstance()
        val emailEditText = findViewById<EditText>(R.id.SignupLogin)
        val passwordEditText = findViewById<EditText>(R.id.SignupPassword)
        val emailPasswordSignupButton = findViewById<Button>(R.id.SignupConfirm)

        emailPasswordSignupButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Check if email and password are not empty
            if (email.isNotEmpty() && password.isNotEmpty()) {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Email/password signup was successful.
                            // You can handle successful signup here.
                            // For example, you can add the user's information to the database.
                            val user = mAuth.currentUser
                            if (user != null) {
                                val email = user.email
                                if (email != null) {
                                    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
                                    val reference: DatabaseReference = db.getReference("Users")
                                    val newUser = reference.push()
                                    newUser.setValue(email)
                                }

                            }
                        } else {
                            // Handle signup failure here (e.g., display an error message).
                            Toast.makeText(this, "Email/password signup failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // Handle empty email or password fields here (e.g., display an error message).
                Toast.makeText(this, "Email and password are required.", Toast.LENGTH_SHORT).show()
            }
        }
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleSignInButton = findViewById<Button>(R.id.Signupconfirm)
        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)

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
                    val user = mAuth.currentUser
                    if (user != null) {
                        val email = user.email
                        if (email != null) {
                            val db: FirebaseDatabase = FirebaseDatabase.getInstance()
                            val reference: DatabaseReference = db.getReference("Users")
                            val newUser = reference.push()
                            newUser.setValue(email)
                        }
                    }
                    // Sign in with Google successful, you can redirect to the main activity
                } else {
                    // Handle Google sign-in failure here
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "SignupActivity"
    }
}
