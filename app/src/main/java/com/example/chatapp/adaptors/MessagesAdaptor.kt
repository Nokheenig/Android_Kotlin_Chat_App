package com.example.chatapp.adaptors

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso

class MessagesAdaptor (
    private val context: Context,
    private val messages: MutableList<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val RECEIVER_TYPE_HOLDER = 1
    private val SENDER_TYPE_HOLDER = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return  if (viewType == RECEIVER_TYPE_HOLDER){
            MeViewHolder(
                LayoutInflater.from(context).inflate(R.layout.me, parent, false)
            )
        } else {
            SenderViewHolder(
                LayoutInflater.from(context).inflate(R.layout.sender, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is MeViewHolder) {
            holder.textViewMessage.text = message.message
        } else if (holder is SenderViewHolder) {
            holder.textViewSender.text = message.message
            //if (message.sender.profileImage.isEmpty()){
            //    holder.senderProfileImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_profile))
            //} else {
            /*
            val imagePath = if(message.sender.profileImage.isEmpty()) null else message.sender.profileImage
            val storageRef : StorageReference = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child(imagePath!!)
            imageRef.downloadUrl.addOnSuccessListener { taskUri ->
                Log.d("test", "banana ${taskUri}")
                Toast.makeText(context, "banana ${taskUri}", Toast.LENGTH_SHORT).show()
                Picasso.get()
                    .load(taskUri)
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.senderProfileImage)
            }.addOnFailureListener {
                Picasso.get()
                    .load(imagePath.toString())
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.senderProfileImage)
            }
             */


            //}
            if (message.sender.profileImage.isEmpty()){
                //holder.senderProfileImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_profile))
                holder.senderProfileImage.setImageResource(R.drawable.ic_profile)
            } else {
                Log.d("test", "Image Uri ${message.sender.profileImage}")
                Toast.makeText(context, "Image Uri ${message.sender.profileImage}", Toast.LENGTH_SHORT).show()
                Picasso.get()
                    .load(message.sender.profileImage)
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.senderProfileImage)
            }

        } else {

        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (FirebaseAuth.getInstance().currentUser?.uid == message.sender.id){
            // The id of the message and the id of the person who is currently logged in are the same
            RECEIVER_TYPE_HOLDER
        } else {
            SENDER_TYPE_HOLDER
        }
    }

    inner class  MeViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val textViewMessage: TextView = view.findViewById(R.id.text_view_me)
    }

    inner class  SenderViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val textViewSender: TextView = view.findViewById(R.id.text_view_sender)
        val senderProfileImage: CircularImageView = view.findViewById(R.id.sender_profile_image)
    }
}