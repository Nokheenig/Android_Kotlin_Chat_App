package com.example.chatapp.model

class ChatMessage (val sender: User, val message: String){
    constructor(): this(User(), "")
}