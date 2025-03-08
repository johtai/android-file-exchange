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

object sendingData {
    lateinit var byteArray: ByteArray
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
        sendingPackeges = 0
        val MaxCountPackages: Int = 5
        allPackeges = 1 + (random() * MaxCountPackages).toInt()
        while(sendingPackeges < allPackeges){
            delay(1000)
            sendingPackeges++
        }

    }
}