package com.example.chatapp.adaptors

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.lang.Exception

class MessagesAdaptor (
    private val context: Context,
    private val messages: MutableList<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val RECEIVER_TYPE_HOLDER = 1
    private val SENDER_TYPE_HOLDER = 2
    private val  IMAGE_TYPE_HOLDER_ME = 3
    private val  IMAGE_TYPE_HOLDER_SENDER = 4

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return  if (viewType == IMAGE_TYPE_HOLDER_ME) {
            ImageHolderMe(
                LayoutInflater.from(context).inflate(R.layout.me_image,parent,false)
            )
        } else if (viewType == IMAGE_TYPE_HOLDER_SENDER) {
            ImageHolderSender(
                LayoutInflater.from(context).inflate(R.layout.sender_image, parent,false)
            )
        } else if (viewType == RECEIVER_TYPE_HOLDER) {
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
        if (holder is ImageHolderMe) {
            if (message.image.isEmpty()){
                Toast.makeText(context, "No image", Toast.LENGTH_LONG).show()
            } else {
                Picasso.get()
                    .load(message.image)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.chat_app)
                    .into(holder.meImage, object : Callback{
                        override fun onSuccess() { }
                        override fun onError(e: Exception?) {
                            //Try again online if cache failed
                            Picasso.get()
                                .load(message.image)
                                .placeholder(R.drawable.chat_app)
                                .into(holder.meImage, object : Callback{
                                    override fun onSuccess() { }
                                    override fun onError(e: Exception?) {
                                        Log.v("Picasso", "Could not fetch image :'(")
                                    }
                                })
                        }
                    })
            }
        }else if (holder is ImageHolderSender) {
            //holder.textViewSender.text = message.message
            holder.textViewSenderUsername.text = message.sender.name
            if (message.sender.profileImage.isEmpty()){
                holder.senderProfileImage.setImageResource(R.drawable.ic_profile)
            } else {
                Picasso.get()
                    .load(message.sender.profileImage)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.senderProfileImage, object : Callback{
                        override fun onSuccess() { }
                        override fun onError(e: Exception?) {
                            //Try again online if cache failed
                            Picasso.get()
                                .load(message.sender.profileImage)
                                .placeholder(R.drawable.ic_profile)
                                .into(holder.senderProfileImage, object : Callback{
                                    override fun onSuccess() { }
                                    override fun onError(e: Exception?) {
                                        Log.v("Picasso", "Could not fetch image :'(")
                                    }
                                })
                        }
                    })
            }
            if (message.image.isEmpty()){
                Toast.makeText(context, "No image", Toast.LENGTH_LONG).show()
            } else {
                Picasso.get()
                    .load(message.image)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.chat_app)
                    .into(holder.senderImage, object : Callback{
                        override fun onSuccess() { }
                        override fun onError(e: Exception?) {
                            //Try again online if cache failed
                            Picasso.get()
                                .load(message.image)
                                .placeholder(R.drawable.chat_app)
                                .into(holder.senderImage, object : Callback{
                                    override fun onSuccess() { }
                                    override fun onError(e: Exception?) {
                                        Log.v("Picasso", "Could not fetch image :'(")
                                    }
                                })
                        }
                    })
            }


        } else if (holder is MeViewHolder) {
            holder.textViewMessage.text = message.message
        } else if (holder is SenderViewHolder) {
            holder.textViewSender.text = message.message
            holder.textViewSenderUsername.text = message.sender.name
            if (message.sender.profileImage.isEmpty()){
                holder.senderProfileImage.setImageResource(R.drawable.ic_profile)
            } else {
                Picasso.get()
                    .load(message.sender.profileImage)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.senderProfileImage, object : Callback{
                        override fun onSuccess() { }
                        override fun onError(e: Exception?) {
                            //Try again online if cache failed
                            Picasso.get()
                                .load(message.sender.profileImage)
                                .placeholder(R.drawable.ic_profile)
                                .into(holder.senderProfileImage, object : Callback{
                                    override fun onSuccess() { }
                                    override fun onError(e: Exception?) {
                                        Log.v("Picasso", "Could not fetch image :'(")
                                    }
                                })
                        }
                    })
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (FirebaseAuth.getInstance().currentUser?.uid == message.sender.id && message.image.isNotEmpty()) {
            IMAGE_TYPE_HOLDER_ME
        } else if (FirebaseAuth.getInstance().currentUser?.uid != message.sender.id && message.image.isNotEmpty()) {
            IMAGE_TYPE_HOLDER_SENDER
        } else if (FirebaseAuth.getInstance().currentUser?.uid == message.sender.id) {
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
        val textViewSenderUsername: TextView = view.findViewById(R.id.sender_username)
    }

    inner class ImageHolderMe(view: View) : RecyclerView.ViewHolder(view) {
        val meImage: ImageView = view.findViewById(R.id.me_image)
    }

    inner class ImageHolderSender(view: View) : RecyclerView.ViewHolder(view) {
        val senderImage: ImageView = view.findViewById(R.id.sender_image)
        val senderProfileImage: CircularImageView = view.findViewById(R.id.sender_profile_image)
        val textViewSenderUsername: TextView = view.findViewById(R.id.sender_username)
    }
}