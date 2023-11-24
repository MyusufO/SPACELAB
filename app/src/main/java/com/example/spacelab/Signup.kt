package com.example.spacelab
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
class Signup : AppCompatActivity() {
    //checking the text link
    fun openlogin(view: View){
        val intent = Intent(this,LoginPage::class.java)
        startActivity(intent)
    }
    private lateinit var mAuth: FirebaseAuth
    private var progressDialog: ProgressDialog? = null
    private var emailVerificationTimer: CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        mAuth = FirebaseAuth.getInstance()
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
        setContentView(R.layout.activity_signup)

        //checking the underlining
        val mTextView = findViewById<TextView>(R.id.txt_Login)
        val mString = "Login"
        val mSpannableString = SpannableString(mString)
        mSpannableString.setSpan(UnderlineSpan(), 0, mSpannableString.length, 0)
        mTextView.text = mSpannableString
        mAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        val emailEditText = findViewById<EditText>(R.id.SignupLogin)
        val passwordEditText = findViewById<EditText>(R.id.SignupPassword)
        val emailPasswordSignupButton = findViewById<Button>(R.id.btn_continue)
        emailPasswordSignupButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (email.isNotEmpty() && isPasswordValid(password)) {
                progressDialog?.setMessage("Creating Account...")
                progressDialog?.show()
                userExistsInYourSystem(email, password)
            }
            else {
                if (!email.isNotEmpty()) {
                    Toast.makeText(this, "Enter an Email", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "Password should contain atleast 6 characters", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Google Sign-In
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val googleSignInButton = findViewById<Button>(R.id.btn_gmail)
        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

    }
    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
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
                            val  userId=user.uid
                            val newUser = reference.child(userId)
                            newUser.child("email").setValue(email)
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)

                        }
                    }
                    // Sign in with Google successful, you can redirect to the main activity
                }
                else {
                    // Handle Google sign-in failure here
                }
            }
    }

    private fun userExistsInYourSystem(email: String, password: String) {
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods
                if (signInMethods == null || signInMethods.isEmpty()) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { createTask ->
                            progressDialog?.dismiss()
                            if (createTask.isSuccessful) {
                                mAuth.currentUser?.sendEmailVerification()
                                    ?.addOnCompleteListener { verificationTask ->
                                        if (verificationTask.isSuccessful) {
                                            // Email verification sent successfully
                                            // Wait for email verification before proceeding
                                            waitForEmailVerificationAndProceed()
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "Failed to send email verification.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            // Navigate to the login page
                                            navigateToLoginPage()
                                        }
                                    }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Email/password signup failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Navigate to the login page
                                navigateToLoginPage()
                            }
                        }
                } else {
                    progressDialog?.dismiss()
                    Toast.makeText(this, "Account already exists.", Toast.LENGTH_SHORT).show()
                    // Navigate to the login page
                    navigateToLoginPage()
                }
            } else {
                progressDialog?.dismiss()
                Toast.makeText(this, "Error checking user existence.", Toast.LENGTH_SHORT).show()
                // Navigate to the login page
                navigateToLoginPage()
            }
        }
    }

    private fun waitForEmailVerificationAndProceed() {
        // Display a loading pop-up while waiting for email verification
        progressDialog?.setMessage("Sending Email Verification...")
        progressDialog?.show()

        // Set up a timer to periodically check email verification status
        emailVerificationTimer = object : CountDownTimer(2000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                checkEmailVerificationStatus()
            }

            override fun onFinish() {
                progressDialog?.dismiss()
                val toast=Toast.makeText(
                    this@Signup,
                    "Please click on the link sent to registered email to verify.",
                    Toast.LENGTH_SHORT
                )
                toast.show()
                // Delay the dismissal of   the toast
                val handler = Handler()
                handler.postDelayed({
                    toast.cancel() // Dismiss the toast after 2 seconds
                    // Navigate to the login page
                    navigateToLoginPage()
                },2500)
            }
        }.start()
    }

    private fun checkEmailVerificationStatus() {
        val user = mAuth.currentUser
        if (user != null && user.isEmailVerified) {
            // Email is verified, proceed to the next activity
            emailVerificationTimer?.cancel()
            progressDialog?.dismiss()
            navigateToLoginPage()
        }
    }

    private fun navigateToLoginPage() {
        val intent = Intent(this, LoginPage::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the timer to avoid leaks
        emailVerificationTimer?.cancel()
    }


    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "SignupActivity"
    }

}