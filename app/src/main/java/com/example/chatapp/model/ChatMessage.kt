package com.example.chatapp.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

class ChatMessage (
    val sender: User,
    val message: String,
    val image: String = "",
    @ServerTimestamp val timeStamp: Date? = null,
){
    constructor(): this(User(), "",  "", null)

    constructor(image: String): this(User(), "", image, null)
    constructor(image: String, user: User ): this(user, "", image, null)
}