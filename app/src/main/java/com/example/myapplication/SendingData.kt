package com.example.myapplication

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.lang.Math.random

val MAX_FILE_SIZE:Int = 1024*1024*50 //50 МБ

object sendingData {
    var byteArray: ByteArray = ByteArray(0)
    var allPackeges by mutableIntStateOf(1)
    var sendingPackeges by mutableIntStateOf(0)
    var requestedPackages by mutableIntStateOf(0)

    fun setData (context: Context, uri: Uri) {
        try {
            byteArray = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }!!
            allPackeges = 1
            sendingPackeges = 0
            requestedPackages = 0
        } catch (e: Exception){
            println(e.message)
        }
    }

    suspend fun sendData(){
        sendingPackages = 0
        val MaxCountPackages: Int = 5
        allPackages = 1 + (random() * MaxCountPackages).toInt()
        while(sendingPackages < allPackages){
            delay(1000)
            sendingPackages++
        }

    }
}