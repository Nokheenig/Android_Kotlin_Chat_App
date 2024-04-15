package com.example.chatapp.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

class ChatMessage (
    val sender: User,
    val message: String,
    val image: String = "",
    @ServerTimestamp val timeStamp: Date? = null,
    var messageId: String? = null
){
    constructor(): this(User(), "",  "", null, null)

    constructor(image: String): this(User(), "", image, null, null)
    constructor(image: String, user: User ): this(user, "", image, null, null)
}