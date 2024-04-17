package com.example.chatapp.ui

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RemoteViews
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ContentView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.adaptors.MessagesAdaptor
import com.example.chatapp.model.ChatMessage
import com.example.chatapp.model.User
import com.example.chatapp.utils.Global
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    private var notificationEnabled: Boolean = false
    private lateinit var storageReference: StorageReference
    private lateinit var uri: Uri
    private lateinit var getResult: ActivityResultLauncher<Intent>
    private lateinit var progressBar: ProgressBar
    private val STORAGE_REQUEST_CODE = 34234
    private var lastListenerDocument: String? = null
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "i.apps.notifications"
    private var notifDescription = "Test notification"
    private var activityInvisible : Boolean = false
    private var activityInvisible2 : Boolean = false
    private var activityInvisible3 : Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("log", "ChatActivity - onCreate (start)")
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

        // it is a class to notify the user of events that happen.
        // This is how you tell the user that something has happened in the
        // background.
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d("log", "ChatActivity - onCreate (end)")
    }

    override fun onStart() {
        Log.d("log", "ChatActivity - onStart (start)")
        super.onStart()
        activityInvisible = false
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

//                                if (messagesAdaptor.isNewMessage(message.messageId!!) && message.sender.id != currentUser.id){
//                                    createNotification(message)
//                                }
                                Log.d("log", if (activityInvisible) "Activity is invisible: TRUE" else "Activity is invisible: FALSE")
                                Log.d("log", if (activityInvisible2) "Activity is invisible2: TRUE" else "Activity is invisible2: FALSE")
                                Log.d("log", if (activityInvisible3) "Activity is invisible3: TRUE" else "Activity is invisible3: FALSE")
                                val actInvisible = isActivityInvisible()
                                Log.d("log", if (actInvisible) "Activity is invisible in majority: TRUE" else "Activity is invisible in majority: FALSE")
                                if (notificationEnabled && actInvisible){
                                    if (messagesAdaptor.isNewMessage(message.messageId!!) && message.sender != currentUser){
                                        val sendNotification = CoroutineScope(Default).launch {
                                            createNotification(message)
                                        }
                                        sendNotification.invokeOnCompletion {
                                            it?.let {
                                                Log.d("log", "createNotification job failed: ${it.message}")
                                            } ?: Log.d("log", "createNotification job succeeded")
                                        }
                                    }
                                }
                                //&& message.sender != currentUser

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
                    if (this::currentUser.isInitialized){
                        notificationEnabled = true
                    }
                }
            }
        CoroutineScope(Default).launch {
            val waitForInit = launch {
                delay(2500)
            }
            waitForInit.join()
            launch {
                if (this@ChatActivity::currentUser.isInitialized){
                    notificationEnabled = true
                }
            }
        }
        Log.d("log", "ChatActivity - onStart (end)")
    }

    override fun onResume() {
        Log.d("log", "ChatActivity - onResume (start)")
        super.onResume()
        activityInvisible = false
        activityInvisible2 = false
        activityInvisible3 = false
        Log.d("log", "ChatActivity - onResume (end)")
    }

    override fun onPause() {
        Log.d("log", "ChatActivity - onPause (start)")
        super.onPause()
        activityInvisible = true
        activityInvisible2 = true
        activityInvisible3 = true
        Log.d("log", "ChatActivity - onPause (end)")
    }

    override fun onStop() {
        Log.d("log", "ChatActivity - onStop (start)")
        super.onStop()
        Log.d("log", "ChatActivity - onStop (end)")
    }

    override fun onDestroy() {
        Log.d("log", "ChatActivity - onDestroy (start)")
        super.onDestroy()
        Log.d("log", "ChatActivity - onDestroy (end)")
    }

    private fun isActivityInvisible():Boolean{
        val res = activityInvisible && activityInvisible2 && activityInvisible3
                || (activityInvisible && activityInvisible2)
                || (activityInvisible && activityInvisible3)
                || (activityInvisible2 && activityInvisible3)
        return res
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
                //notificationEnabled = true
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
                notificationEnabled = false
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_REQUEST_CODE && grantResults.size > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getImage()
        } else {
            Toast.makeText(this@ChatActivity, "Permission not granted", Toast.LENGTH_LONG).show()
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

    private suspend fun createNotification(message: ChatMessage) {
        // pendingIntent is an intent for future use i.e after
        // the notification is clicked, this intent will come into action
        val intent = Intent(this, ChatActivity::class.java)
        var notifImage : Bitmap? = null
        // FLAG_UPDATE_CURRENT specifies that if a previous
        // PendingIntent already exists, then the current one
        // will update it with the latest intent
        // 0 is the request code, using it later with the
        // same method again will get back the same pending
        // intent for future reference
        // intent passed here is to our afterNotification class
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        /*
        // Deprecated:
        val icon: Int = R.drawable.ic_launcher_background
        val notifTime: Long = System.currentTimeMillis()
        val notification = Notification(icon,"Custom Notification", notifTime)
         */

        // RemoteViews are used to use the content of
        // some different layout apart from the current activity layout
        val contentView = RemoteViews(packageName, R.layout.custom_layout_notification)

        val setupNotification = CoroutineScope(Default).launch{
            val setNotifTime = launch{
                contentView.setTextViewText(R.id.notification_time, convertTime(System.currentTimeMillis()))
            }
            val setNotifTitle = launch {
                contentView.setTextViewText(R.id.notification_title, "${message.sender.name}:")
            }
            val setNotifMessage = launch {
                contentView.setTextViewText(R.id.notification_text, message.message)
            }
            val setNotifProfileImage = launch {
                val notifProfileImage : Deferred<Any> = async {
                    val resBitmap: Any = if (message.sender.profileImage.isEmpty()){
                        R.drawable.ic_profile
                    } else {
                        Picasso.get()
                            .load(message.sender.profileImage)
                            .placeholder(R.drawable.ic_profile).get()
                    }
                    resBitmap
                }
                val res = notifProfileImage.await()
                if (res is Bitmap){
                    contentView.setImageViewBitmap(R.id.notification_profile_image, res)
                } else if (res is Int){
                    contentView.setImageViewResource(R.id.notification_profile_image, res)
                }


            }
            val setNotifImage = launch {
                val notifImage : Deferred<Any> = async {
                    val resBitmap: Any = if (message.image.isEmpty()){
                        contentView.setViewVisibility(R.id.notification_image, View.GONE)
                        contentView.setViewVisibility(R.id.notification_text, View.VISIBLE)

                        R.drawable.chat_app
                    } else {
                        contentView.setViewVisibility(R.id.notification_image, View.VISIBLE)
                        contentView.setViewVisibility(R.id.notification_text, View.GONE)

                        Picasso.get()
                            .load(message.image)
                            .placeholder(R.drawable.chat_app).get()
                    }
                    resBitmap
                }
                val res = notifImage.await()
                if (res is Bitmap){
                    contentView.setImageViewBitmap(R.id.notification_image, res)
                } else if (res is Int) {
                    contentView.setImageViewResource(R.id.notification_image, res)
                }

            }
        }
        setupNotification.join()
        setupNotification.invokeOnCompletion {
            it?.let {
                Log.d("log", "Notification setup FAILED: ${it.message}")
            } ?: run {
                Log.d("log", "Notification setup succeeded, sending notification...")
                // checking if android version is greater than oreo(API 26) or not
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationChannel = NotificationChannel(channelId, notifDescription, NotificationManager.IMPORTANCE_HIGH)
                    notificationChannel.enableLights(true)
                    notificationChannel.lightColor = Color.GREEN
                    notificationChannel.enableVibration(false)
                    notificationManager.createNotificationChannel(notificationChannel)

                    builder = Notification.Builder(this@ChatActivity, channelId)
                        .setContent(contentView)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setLargeIcon(BitmapFactory.decodeResource(this@ChatActivity.resources, R.drawable.ic_launcher_background))
                        .setContentIntent(pendingIntent)
                } else {

                    builder = Notification.Builder(this@ChatActivity)
                        .setContent(contentView)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setLargeIcon(BitmapFactory.decodeResource(this@ChatActivity.resources, R.drawable.ic_launcher_background))
                        .setContentIntent(pendingIntent)
                }
                notifImage?.let {
                    builder.setStyle(Notification.BigPictureStyle().bigPicture(it))
                }
                notificationManager.notify(1234, builder.build())
            }
        }
        /*
        val myBitmap: Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.chat_app)

        if (message.sender.profileImage.isEmpty()){
            contentView.setImageViewResource(R.id.notification_profile_image,R.drawable.ic_profile)
        } else {
            //contentView.setImageViewResource(R.id.notification_profile_image,R.drawable.ic_profile_green)

            CoroutineScope(Dispatchers.IO).launch {
                delay(2000)
            }
            val notifProfileImage : Bitmap = Picasso.get()
                .load(message.sender.profileImage)
                .placeholder(R.drawable.ic_profile).get()
            contentView.setImageViewBitmap(R.id.notification_profile_image, notifProfileImage)

            /*Picasso.get()
                .load(message.sender.profileImage)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(findViewById(R.id.notification_profile_image), object : Callback {
                    override fun onSuccess() { }
                    override fun onError(e: Exception?) {
                        //Try again online if cache failed
                        Picasso.get()
                            .load(message.sender.profileImage)
                            .placeholder(R.drawable.ic_profile)
                            .into(findViewById(R.id.notification_profile_image), object : Callback {
                                override fun onSuccess() { }
                                override fun onError(e: Exception?) {
                                    Log.v("Picasso", "Could not fetch image :'(")
                                }
                            })
                    }
                })
             */
        }

        contentView.setTextViewText(R.id.notification_title, "${message.sender.name}:")
        if (message.image.isNotEmpty()){
            //contentView.setViewVisibility(R.id.notification_image, View.VISIBLE)
            contentView.setViewVisibility(R.id.notification_text, View.GONE)
            //contentView.setImageViewResource(R.id.notification_image, R.drawable.ic_profile_green)

            val notifImage: Bitmap = Picasso.get()
                .load(message.image)
                .placeholder(R.drawable.chat_app).get()
                //.into(imageViewNotifImage)
            contentView.setImageViewBitmap(R.id.notification_image, notifImage)
            /*
            Picasso.get()
                .load(message.image)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(findViewById(R.id.notification_image), object : Callback {
                    override fun onSuccess() { }
                    override fun onError(e: Exception?) {
                        //Try again online if cache failed
                        Picasso.get()
                            .load(message.image)
                            .placeholder(R.drawable.chat_app)
                            .into(findViewById(R.id.notification_image), object : Callback {
                                override fun onSuccess() { }
                                override fun onError(e: Exception?) {
                                    Log.v("Picasso", "Could not fetch image :'(")
                                }
                            })
                    }
                })

             */
        } else {
            contentView.setViewVisibility(R.id.notification_image, View.GONE)
            contentView.setViewVisibility(R.id.notification_text, View.VISIBLE)
            contentView.setTextViewText(R.id.notification_text, message.message)
        }
        contentView.setTextViewText(R.id.notification_text, message.message)
         */
        /*
        // checking if android version is greater than oreo(API 26) or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, notifDescription, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, channelId)
                .setContent(contentView)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_launcher_background))
                .setContentIntent(pendingIntent)
        } else {

            builder = Notification.Builder(this)
                .setContent(contentView)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_launcher_background))
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(1234, builder.build())
         */
    }

    private fun convertTime(time: Long) : String {
        val date = Date(time)//*1000L)
        val timeFormatted = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.UK)
        timeFormatted.timeZone = TimeZone.getDefault()
        return timeFormatted.format(date)
    }
}