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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
            val boool:Boolean=email.isNotEmpty()
            if (boool && isPasswordValid(password)) {
                userExistsInYourSystem(email, password)

                Toast.makeText(this,"Success",Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show()
            }
        }

        // Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val googleSignInButton = findViewById<Button>(R.id.Signupconfirm)

        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        val bool:Boolean=password.length>=6
        return bool
    }

    private fun userExistsInYourSystem(email: String, callback: (Boolean) -> Unit) {
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val path = "Users/$email"
        val reference: DatabaseReference = db.getReference(path)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Handle the data from the dataSnapshot
                val value = dataSnapshot.getValue(String::class.java)
                val userExists = value != null
                callback(userExists)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
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
                            val reference: DatabaseReference = db.getReference("GoogleUsers")
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
    fun userExistsInYourSystem(email: String, password: String) {
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

        // Check if the user already exists
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods
                if (signInMethods == null || signInMethods.isEmpty()) {
                    // User does not exist, proceed with account creation
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { createTask ->
                            if (createTask.isSuccessful) {
                                val user = mAuth.currentUser
                                if (user != null) {
                                    val userEmail = user.email
                                    if (userEmail != null) {
                                        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
                                        val reference: DatabaseReference = db.getReference("Users")
                                        val newUser = reference.push()
                                        newUser.setValue(userEmail)

                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                    }
                                }
                            }
                            else {
                                Toast.makeText(this, "Email/password signup failed.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                else {
                    // User already exists
                    Toast.makeText(this, "Account already exists.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error checking user existence.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "SignupActivity"
    }
}
