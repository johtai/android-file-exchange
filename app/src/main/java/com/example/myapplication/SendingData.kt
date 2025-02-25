package com.example.myapplication

import android.content.Context
import android.net.Uri

class SendingData() {
    lateinit var byteArray: ByteArray

    constructor(context: Context, uri: Uri) : this() {
        try {
            byteArray = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }!!
        } catch (e: Exception){
            println(e.message)
        }


    }
}