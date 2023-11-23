package com.example.spacelab

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.spacelab.databinding.ActivityLoginPageBinding
import com.example.spacelab.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class LoginPage : AppCompatActivity() {

    //checking the text link
    fun opensignup(view: View){
        //First check if the user logged in is verified
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        if(currentUser!=null) {
            val user: FirebaseUser? = mAuth.getCurrentUser()
            if (user != null) {
                if (!user.isEmailVerified){
                    mAuth.signOut()
                }
            }
        }
        val intent = Intent(this,Signup::class.java)
        startActivity(intent)
    }

    //checking forgotpass text link
    fun openforgotpass(view: View){
        val intent = Intent(this,ForgotPass::class.java)
        startActivity(intent)
    }

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)
        //val intent=Intent(this,TaskPage::class.java)
        //startActivity(intent)
        //finish()
        //checking the underlining
        val mTextView = findViewById<TextView>(R.id.txt_Signup)
        val mString = "Sign Up"
        val mSpannableString = SpannableString(mString)
        mSpannableString.setSpan(UnderlineSpan(), 0, mSpannableString.length, 0)
        mTextView.text = mSpannableString

        mAuth = FirebaseAuth.getInstance()
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleSignInButton = findViewById<Button>(R.id.btn_google)
        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        val emailEditText = findViewById<EditText>(R.id.LoginEmail)
        val passwordEditText = findViewById<EditText>(R.id.LoginPassword)
        val emailPasswordLoginButton = findViewById<Button>(R.id.btn_login)

        emailPasswordLoginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if(email.equals("")||password.length<6){
                Toast.makeText(this, "$password", Toast.LENGTH_SHORT).show()
            }
            else{
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user: FirebaseUser? = mAuth.getCurrentUser()
                            if(user?.isEmailVerified == true) {
                                val db: FirebaseDatabase = FirebaseDatabase.getInstance()
                                val reference: DatabaseReference = db.getReference("Users")
                                reference.get().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val dataSnapshot = task.result
                                        if (dataSnapshot != null) {

                                            for (snapshot in dataSnapshot.children) {
                                                val userKey = snapshot.key
                                                val userEmail = snapshot.child("email").getValue()

                                                if (userEmail == email) {
                                                    val reference1 =
                                                        db.getReference("Users/$userKey")
                                                    reference1.addListenerForSingleValueEvent(object :
                                                        ValueEventListener {
                                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                            val intent = Intent(
                                                                this@LoginPage,
                                                                MainActivity::class.java
                                                            )
                                                            intent.putExtra("key", userKey)
                                                            startActivity(intent)

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
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            }
                            else{
                                sendEmailVerification()
                            }
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()

                        }
                    }
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
                Log.w(TAG, "Google sign-in failed", e)
            }
        }
    }
    private fun sendEmailVerification() {
        val user = FirebaseAuth.getInstance().currentUser
        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Email sent successfully, inform the user
                    Toast.makeText(
                        applicationContext,
                        "Verification email sent",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else {
                    // If sending the email verification fails, display a message to the user.
                    Toast.makeText(
                        applicationContext,
                        "Failed to send verification email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onStop() {
        super.onStop()
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        if(currentUser!=null) {
            val user: FirebaseUser? = mAuth.getCurrentUser()
            if (user != null) {
                if (!user.isEmailVerified){
                    mAuth.signOut()
                }
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
                    finish()
                } else {
                    // Handle Google sign-in failure here
                    Log.e(TAG, "Google sign-in failed", task.exception)
                    // Display an error message or handle the failure in some way
                    Toast.makeText(
                        this,
                        "Google Sign-In failed Check if this mail is signed up ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "LoginPage"
    }
}
