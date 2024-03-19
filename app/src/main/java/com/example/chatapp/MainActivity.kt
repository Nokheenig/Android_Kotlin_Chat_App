package com.example.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.chatapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        mBinding.signInButton.setOnClickListener {

        }
        mBinding.signUpButton.setOnClickListener {

        }
        mBinding.textViewRegister.setOnClickListener{
            mBinding.flipper.setInAnimation(this,android.R.anim.slide_in_left)
            mBinding.flipper.setOutAnimation(this,android.R.anim.slide_out_right)
            mBinding.flipper.showNext()
        }
        mBinding.textViewSignIn.setOnClickListener{
            mBinding.flipper.setInAnimation(this,android.R.anim.slide_out_right)
            mBinding.flipper.setOutAnimation(this,android.R.anim.slide_in_left)
            mBinding.flipper.showPrevious()
        }
    }
}