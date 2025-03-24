package com.example.myapplication

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import java.lang.Math.random
import java.io.File


val MAX_FILE_SIZE:Int = 1024*1024*50 //50 МБ

object sendingData {
    var filename:String = ""
    lateinit var byteArray: List<ByteArray>
    var allPackages by mutableIntStateOf(1)
    var sendingPackages by mutableIntStateOf(0)
    var requestedPackages by mutableIntStateOf(0)
    var address: String = ""
    var port: Int = 0

    fun setData (context: Context, uri: Uri) {
        try {
            //byteArray = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            //println(uri.toString())
            byteArray = splitFile(context,  uri.toString())
            filename = File(uri.path!!).extension;
            println(filename)
            allPackages = byteArray.size
            sendingPackages = 0
            requestedPackages = 0
        } catch (e: Exception){
            println(e.message)
        }

    }

    suspend fun sendData(){
        val MaxCountPackages: Int = 5
        sendingPackages = 0
        allPackages = 1 + (random() * MaxCountPackages).toInt()
        while(sendingPackages < allPackages){
            delay(1000)
            sendingPackages++
        }
    }

    fun splitFile(context: Context, path: String, chunkSize: Int = 65507): List<ByteArray> {
        //val file = File(path)
        val parts = mutableListOf<ByteArray>()
        val buffer = ByteArray(chunkSize)

        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(path.toUri())

        inputStream?.use { stream ->
            while(true)
            {
                val bytesRead = stream.read(buffer) //stream.readBytes()
                if(bytesRead == -1 ) break
                parts.add(buffer.copyOf(bytesRead))
            }

        }

        return parts
    }
}
