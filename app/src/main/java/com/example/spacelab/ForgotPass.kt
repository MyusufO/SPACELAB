package com.example.spacelab

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth

class ForgotPass : AppCompatActivity() {

    private lateinit var btnReset: Button
    private lateinit var btnBack: Button
    private lateinit var edtEmail: EditText
    private lateinit var mAuth: FirebaseAuth
    private lateinit var strEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)

        btnBack = findViewById(R.id.btnBack)
        btnReset = findViewById(R.id.btnReset)
        edtEmail = findViewById(R.id.editBox)

        mAuth = FirebaseAuth.getInstance()

        // Reset Button Listener
        btnReset.setOnClickListener {
            strEmail = edtEmail.text.toString().trim()
            if (!TextUtils.isEmpty(strEmail)) {
                resetPassword()
            } else {
                edtEmail.error = "Email field can't be empty"
            }
        }

        // Back Button Code
        btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun resetPassword() {
        btnReset.visibility = View.INVISIBLE

        mAuth.sendPasswordResetEmail(strEmail)
            .addOnSuccessListener(OnSuccessListener {
                Toast.makeText(
                    this@ForgotPass,
                    "Reset Password link has been sent to your registered email",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this@ForgotPass, LoginPage::class.java)
                startActivity(intent)
                finish()
            })
            .addOnFailureListener(OnFailureListener { e ->
                Toast.makeText(
                    this@ForgotPass,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                btnReset.visibility = View.VISIBLE
            })
    }
}