package com.example.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.chatapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        mBinding.signInButton.setOnClickListener {
            signIn()
        }
        mBinding.signUpButton.setOnClickListener {
            createAccount()
        }
        mBinding.textViewRegister.setOnClickListener{
            startNextAnimation()
        }
        mBinding.textViewSignIn.setOnClickListener{
            startPreviousAnimation()
        }
        mBinding.textViewGoToProfile.setOnClickListener {
            startNextAnimation()
        }
        mBinding.textViewSignUp.setOnClickListener {
            startPreviousAnimation()
        }
    }

    private fun signIn() {
        val email = mBinding.signInInputEmail.editText?.text.toString().trim()
        val password = mBinding.signInInputPassword.editText?.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "You should provide an email and a password", Toast.LENGTH_LONG).show()
            return
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "User signed in", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Couldn't sign in\nSomething went wrong.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun createAccount() {
        val email = mBinding.signUpInputEmail.editText?.text.toString().trim()
        val password = mBinding.signUpInputPassword.editText?.text.toString().trim()
        val confirmPassword = mBinding.signUpInputConfirmPassword.editText?.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "You should provide an email and a password", Toast.LENGTH_LONG).show()
            return
        }

        if (password != confirmPassword){
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_LONG).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account created", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "The account wasn't created:\n${task.exception}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun startNextAnimation() {
        mBinding.flipper.setInAnimation(this,android.R.anim.slide_in_left)
        mBinding.flipper.setOutAnimation(this,android.R.anim.slide_out_right)
        mBinding.flipper.showNext()
    }

    private fun startPreviousAnimation() {
        mBinding.flipper.setInAnimation(this,android.R.anim.slide_in_left)
        mBinding.flipper.setOutAnimation(this,android.R.anim.slide_out_right)
        mBinding.flipper.showPrevious()
    }
}