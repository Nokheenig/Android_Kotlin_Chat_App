package com.example.chatapp.utils

import android.app.Application
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso


class Global : Application() {
    override fun onCreate() {
        super.onCreate()
        val builder = Picasso.Builder(this)
        builder.downloader(OkHttp3Downloader(this, Int.MAX_VALUE.toLong()))
        val built = builder.build()
        built.setIndicatorsEnabled(true)
        built.isLoggingEnabled = true
        Picasso.setSingletonInstance(built)
    }
}