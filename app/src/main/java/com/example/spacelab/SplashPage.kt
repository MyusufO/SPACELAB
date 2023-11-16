package com.example.spacelab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.content.Intent


class SplashPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_page)

        Handler().postDelayed({
            val iHome=Intent(this@SplashPage,LoginPage::class.java)
            startActivity(iHome)
            finish()
        },3000)
    }
}