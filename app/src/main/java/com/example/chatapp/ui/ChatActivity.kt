package com.example.chatapp.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.adaptors.MessagesAdaptor
import com.example.chatapp.model.ChatMessage
import com.example.chatapp.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ChatActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val usersRef: CollectionReference = db.collection("users_collection")
    private val messagesRef: CollectionReference = db.collection("messages_collection")
    private lateinit var sendButton: Button
    private lateinit var editTextMessage: EditText
    private lateinit var messagesAdaptor: MessagesAdaptor
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messages: MutableList<ChatMessage>
    private lateinit var currentUser: User
    private lateinit var storageReference: StorageReference
    private lateinit var uri: Uri
    private lateinit var getResult: ActivityResultLauncher<Intent>
    private lateinit var progressBar: ProgressBar
    private val STORAGE_REQUEST_CODE = 34234
    private var lastListenerDocument: String? = null
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        //Toast.makeText(this@ChatActivity, FirebaseAuth.getInstance().currentUser?.uid, Toast.LENGTH_LONG).show()

        messagesRecyclerView = findViewById(R.id.message_recycler_view)
        sendButton = findViewById(R.id.send_message_button)
        editTextMessage = findViewById(R.id.input_message)
        progressBar = findViewById(R.id.progressBarChatAct)
        storageReference = FirebaseStorage.getInstance().reference

        initRecyclerView()
        getCurrentUser()

        sendButton.setOnClickListener {
            insertMessage()
        }

        editTextMessage.setOnTouchListener { _, event ->
            val DRAWABLE_RIGHT = 2
            val DRAWABLE_LEFT = 0
            val DRAWABLE_TOP = 1
            val DRAWABLE_BOTTOM = 3
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (editTextMessage.right - editTextMessage.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                    editTextMessage.setText("")
                    if (ActivityCompat.checkSelfPermission(
                            this@ChatActivity,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermission()
                    } else {
                        getImage()
                    }
                }
            }
            false
        }

        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == RESULT_OK) {
                uri = it.data?.data!!
                hideProgressBar()
                uploadImage()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        messages.clear()
        messagesRef.orderBy("timeStamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                error?.let {
                    return@addSnapshotListener
                }
                snapshots?.let {
                    for (dc in it.documentChanges) {
                        if (lastListenerDocument != null){
                            if (dc.document.id == lastListenerDocument){
                                continue
                            }
                        }
                        lastListenerDocument = dc.document.id

                        // Because we need indexes in order to add animations:
                        val oldIndex = dc.oldIndex
                        val newIndex = dc.newIndex

                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                Log.d("duplicateTest","New document added to Firestore: ${dc.document.id}\nContent:\n${dc.document.data}")
                                val snapshot = dc.document
                                val message = snapshot.toObject(ChatMessage::class.java)
                                message.messageId = snapshot.id

                                messages.add(newIndex, message)
                                messagesAdaptor.notifyItemInserted(newIndex) // <- This is gonna add an animation whenever we add a new message
                                //messagesAdaptor.notifyDataSetChanged()
                                messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
                            }

                            DocumentChange.Type.REMOVED -> {

                            }

                            DocumentChange.Type.MODIFIED -> {

                            }
                        }

                    }
                }
            }
    }

    private fun initRecyclerView() {
        messages = mutableListOf()
        messagesAdaptor = MessagesAdaptor(this, messages)
        messagesRecyclerView.adapter = messagesAdaptor
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.setHasFixedSize(true)
    }

    private fun getCurrentUser() {
        usersRef.whereEqualTo("id", FirebaseAuth.getInstance().currentUser?.uid)
            .get()
            .addOnSuccessListener {
                for (snapshot in it) {
                    currentUser = snapshot.toObject(User::class.java)
                }
            }

    }

    private fun insertMessage() {
        val message = editTextMessage.text.toString()

        if (message.isNotEmpty()) {
            messagesRef.document()
                .set(ChatMessage(currentUser, message))
                .addOnSuccessListener {
                    editTextMessage.setText("")
                }
            // Here we don't have to add onSuccessListener and onFailure Listener because we will see directly in the UI if inserting the message succeeded  or not
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_sign_out -> {

                FirebaseAuth.getInstance().signOut()
                Intent(this@ChatActivity, MainActivity::class.java).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // Added with Splashscreen : necessary ?
                    startActivity(it)
                    finish()
                }
                return true
            }
        }
        return false
    }

    private  fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this@ChatActivity,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)){
            // This function checks if we should show the user why we need this permission,
            // and this happens if the user denies the permissions before and tries to access again a functionnality requiring it
            //Log.d("dBug", "test if shouldShowRequestPermissionRationale case")
            AlertDialog.Builder(this@ChatActivity)
                .setPositiveButton(R.string.dialog_button_yes) { _,_ ->
                    ActivityCompat.requestPermissions(this@ChatActivity,
                        arrayOf(
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        ),
                        STORAGE_REQUEST_CODE)
                    getImage()
                }.setNegativeButton(R.string.dialog_button_no){
                        dialog, _ ->
                    dialog.cancel()
                }.setTitle("Permission needed")
                .setMessage("This permission is needed for accessing the internal storage.")
                .show()
        } else {
            ActivityCompat.requestPermissions(this@ChatActivity,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                STORAGE_REQUEST_CODE)
        }
    }

    private fun getImage(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        getResult.launch(intent)
    }

    private fun uploadImage() {
        if (this::uri.isInitialized) {
            showProgressBar()

            val filePath = storageReference.child("chat_images")
                .child("${System.currentTimeMillis()}.image")
            filePath.putFile(uri).addOnCompleteListener { imageUploadTask ->
                if (!imageUploadTask.isSuccessful){
                    Toast.makeText(this@ChatActivity, "Failed to upload the image: ${imageUploadTask}", Toast.LENGTH_LONG).show()
                } else {
                    // Image successfully uploaded
                    //Toast.makeText(this@ChatActivity, "Image uploaded successfully", Toast.LENGTH_LONG).show()
                    imageUploadTask.result.storage.downloadUrl.addOnCompleteListener {
                        val imageUrl = if (it.isSuccessful) {
                            // Image downloadUrl successfully retrieved
                            it.result.toString()
                        } else {
                            ""
                        }

                        //Toast.makeText(this@ChatActivity, it.toString(), Toast.LENGTH_SHORT).show()
                        val message = ChatMessage(
                            imageUrl,
                            currentUser
                        )
                        messagesRef.document()
                            .set(message)
                            .addOnCompleteListener {
                                if (it.isSuccessful){
                                    //Toast.makeText(this, "Image message was successfully added in database!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Image message failed to insert in database !", Toast.LENGTH_SHORT).show()
                                }
                                hideProgressBar()
                            }
                    }
                }
            }
        }
    }

    private fun showProgressBar(){
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar(){
        progressBar.visibility = View.GONE
    }
}