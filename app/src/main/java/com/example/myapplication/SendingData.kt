package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.ByteReadPacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.io.readByteArray
import kotlinx.io.readString
import java.lang.Math.random
import java.io.File
import java.io.FileOutputStream
import java.util.UUID


val MAX_FILE_SIZE:Int = 1024*1024*50 //50 МБ

object sendingData {
    var filename = "Unknown"
    var byteArray: List<ByteArray> =  List<ByteArray>(size = 0, { byteArrayOf(3)})
    var allPackages by mutableIntStateOf(1)
    var sendingPackages by mutableIntStateOf(0)
    var requestedPackages by mutableIntStateOf(0)

    fun setData (context: Context, uri: Uri) {
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        filename = cursor.getString(nameIndex)
                    }
                }
            }
            byteArray = splitFile(context,  uri.toString())
            allPackages = 0
            sendingPackages = 0
            requestedPackages = 0
        } catch (e: Exception){
            println(e.message)
        }

    }

    suspend fun sendData(ip: String, port: Int, file: List<ByteArray>){

        sendingPackages = 0
        allPackages = byteArray.size

        try {
            val selectorManager = SelectorManager(Dispatchers.IO)
            val socket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", 5088))
            val clientId = UUID.randomUUID().toString() // генерируем уникальный идентификатор

            while(true) {
                socket.send(Datagram(packet = ByteReadPacket(clientId.encodeToByteArray()), address = InetSocketAddress(ip, port)))
                println("packet with $clientId was sent")
                break
            }


            while (true)
            {
                val filename = sendingData.filename
                socket.send(Datagram(ByteReadPacket(filename.encodeToByteArray()), InetSocketAddress(ip, port)))
                val packet = socket.receive()
                if (packet.packet.readString() == filename)
                {
                    println("Название дошло успешно")
                    break;
                }
            }

            while (true)
            {
                socket.send(Datagram(ByteReadPacket(file.size.toString().encodeToByteArray()), InetSocketAddress(ip, port)))
                val packet = socket.receive()
                if (packet.packet.readString() == file.size.toString())
                {
                    println("Размер (в пакетах) дошёл успешно")
                    break;
                }
            }

            for (i in 0..<file.size)
            {
                delay(90)
                socket.send(Datagram(ByteReadPacket(file[i]), InetSocketAddress(ip, port)))
                println("$i пакет отправлен")
                ++sendingData.sendingPackages
                val packet = socket.receive()
                val message = packet.packet.readString()
                if (message == i.toString())
                {
                    println("$i пакет дошёл до сервера")
                }
                else
                {
                    socket.send(Datagram(ByteReadPacket(file[i]), InetSocketAddress(ip, port)))
                }
            }
            socket.close()

        } catch (e:Exception) {
            println("Ошибка: "+ e.message)
        }
    }

    suspend fun receiveData(ip: String, port: Int){
        filename = "Unknown"
        byteArray = List<ByteArray>(size = 0, { byteArrayOf(3)})
        allPackages = 0
        sendingPackages = 0
        requestedPackages = 0
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", 5089))

        socket.send(
            Datagram(
                packet = ByteReadPacket(array = "Здесь будет JVT токен".encodeToByteArray()),
                address = InetSocketAddress(ip, port)
            )
        )
        var packet = socket.receive()
        filename = packet.packet.readString()
        socket.send(
            Datagram(
                packet = ByteReadPacket(array = filename.encodeToByteArray()),
                address = packet.address
            )
        )
        println("File name received: $filename")

        packet = socket.receive()
        val messageCount = packet.packet.readString()
        socket.send(
            Datagram(
                packet = ByteReadPacket(array = messageCount.encodeToByteArray()),
                address = packet.address
            )
        )
        println("messageCount: $messageCount")

        val listBytes = mutableListOf<ByteArray>()

        for (i in 0 ..<messageCount.toInt()) {
            packet = socket.receive()
            println("packet:$packet")
            val numMessage = packet.packet.readByteArray(4)
            val numOfPacket = (numMessage[3].toInt() shl 24) or
                    (numMessage[2].toInt() and 0xff shl 16) or
                    (numMessage[1].toInt() and 0xff shl 8) or
                    (numMessage[0].toInt() and 0xff)

            val dataMessage = packet.packet.readByteArray()

            println("nop:$numOfPacket")
            listBytes.add(dataMessage)
            socket.send(
                Datagram(
                    packet = ByteReadPacket(
                        array = numOfPacket.toString().encodeToByteArray()
                    ), address = packet.address
                )
            )
        }
        socket.close()
        saveFile(filename, listBytes)
    }


    fun splitFile(context: Context, path: String, chunkSize: Int = 1428): List<ByteArray> {
        //val file = File(path)
        val parts = mutableListOf<ByteArray>()
        val buffer = ByteArray(chunkSize)
        val bufferNum = ByteArray(4)

        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(path.toUri())

        var i = 0

        inputStream?.use { stream ->
            while(true)
            {
                val bytesRead = stream.read(buffer) //stream.readBytes()
                if (bytesRead == -1) break
                for (j in 0..3) bufferNum[j] = (i shr (j*8)).toByte()
                ++i
                parts.add(bufferNum.copyOf(4) + buffer.copyOf(bytesRead))
            }
        }
        return parts
    }

    fun saveFile(filename:String, listByteArray: List<ByteArray>)  {
        val file = FileOutputStream(filename)
        for (i in listByteArray)
            file.write(i)
        file.close()
    }
}
