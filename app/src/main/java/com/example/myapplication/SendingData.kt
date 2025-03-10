package com.example.myapplication

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.lang.Math.random

val MAX_FILE_SIZE:Int = 1024*1024*50 //50 МБ

object sendingData {
    var byteArray: ByteArray = ByteArray(0)
    var allPackages by mutableIntStateOf(1)
    var sendingPackages by mutableIntStateOf(0)
    var requestedPackages by mutableIntStateOf(0)

    fun setData (context: Context, uri: Uri) {
        try {
            byteArray = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }!!
            allPackages = 1
            sendingPackages = 0
            requestedPackages = 0
        } catch (e: Exception){
            println(e.message)
        }
    }

    suspend fun sendData(){
        val MaxCountPackages: Int = 5
        sendingPackages=0
        allPackages = 1 + (random() * MaxCountPackages).toInt()
        while(sendingPackages < allPackages){
            delay(1000)
            sendingPackages++
        }

    }
}