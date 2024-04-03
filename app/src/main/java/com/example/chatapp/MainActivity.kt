package com.example.chatapp

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.chatapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var getResult: ActivityResultLauncher<Intent>
    private val STORAGE_REQUEST_CODE = 1293487
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
        mBinding.profileImage.setOnClickListener{
            if (ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermission()
            } else {
                getImage()
            }
        }

        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == RESULT_OK) {
                mBinding.profileImage.setImageURI(it.data?.data)
            }
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

    private fun getImage(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getResult.launch(intent)
    }

    private  fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)){
            // This function checks if we should show the user why we need this permission,
            // and this happens if the user denies the permissions before and tries to access again a functionnality requiring it
            //Log.d("dBug", "test if shouldShowRequestPermissionRationale case")
            AlertDialog.Builder(this@MainActivity)
                .setPositiveButton(R.string.dialog_button_yes) { _,_ ->
                    ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        ),
                        STORAGE_REQUEST_CODE)
                }.setNegativeButton(R.string.dialog_button_no){
                    dialog, _ ->
                    dialog.cancel()
                }.setTitle("Permission needed")
                .setMessage("This permission is needed for accessing the internal storage.")
                .show()
        } else {
            ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                STORAGE_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getImage()
        } else {
            Toast.makeText(this@MainActivity, "Permission not granted", Toast.LENGTH_LONG).show()
        }
    }
}