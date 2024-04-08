package com.example.chatapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.chatapp.R
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        Toast.makeText(this@ChatActivity, FirebaseAuth.getInstance().currentUser?.uid, Toast.LENGTH_LONG).show()
    }
}