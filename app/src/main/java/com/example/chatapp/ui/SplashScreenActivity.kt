package com.example.chatapp.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.os.postDelayed
import com.example.chatapp.MainActivity
import com.example.chatapp.R

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler(Looper.getMainLooper()).postDelayed(1800) {
            ////////////////////////////////////////////////////////////////////////////////////////////////
            /*
            shared preference code for keeping users logged IN.
            shared preferences are local data storage system to store data with key and value.
             */
            val pref = getSharedPreferences("logIn", Context.MODE_PRIVATE)
            val isLoggedIn = pref.getBoolean("isLoggedIn", false)
            ////////////////////////////////////////////////////////////////////////////////////////////////

            //Decide whether to start LogInPage or HomePage as per the user logIn status
            val targetActivity = if (isLoggedIn) ChatActivity::class.java else MainActivity::class.java
            startActivity(Intent(this, targetActivity))
            finish()
        }
    }
}