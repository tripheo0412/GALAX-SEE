package com.example.tripheo2410.galaxsee.splashScreen

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.tripheo2410.galaxsee.R
import com.example.tripheo2410.galaxsee.mainActivity.MainActivity


class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val myIntent = Intent(this, MainActivity::class.java)
        this.startActivity(myIntent)
    }
}