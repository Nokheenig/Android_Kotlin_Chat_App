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
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed(1800) {

            auth = FirebaseAuth.getInstance()

            auth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                val targetActivity = if (user != null) {
                    // User is signed in.
                    ChatActivity::class.java
                } else {
                    // User is signed out.
                    MainActivity::class.java
                }
                startActivity(Intent(this, targetActivity))
                finish()
            }
        }
    }
}