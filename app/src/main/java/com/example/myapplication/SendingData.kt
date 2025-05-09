package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.ByteReadPacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.io.readByteArray
import kotlinx.io.readString
import java.util.UUID


const val MAX_FILE_SIZE:Int = 1024*1024*50 //50 МБ

object sendingData {
    var filename = "Unknown"
    var byteArray: List<ByteArray> =  List<ByteArray>(size = 0, { byteArrayOf(3)})
    var allPackages by mutableIntStateOf(1)
    var sentPackages by mutableIntStateOf(0)
    var resentPackages by mutableIntStateOf(0)
    var ip by mutableStateOf("5.165.249.136")
    var port by mutableIntStateOf(2869)
    var downloadingInProcess by mutableStateOf(true)
    var receiveDataDialog by mutableStateOf(false)

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
            sentPackages = 0
            resentPackages = 0
        } catch (e: Exception){
            println(e.message)
        }
    }

    suspend fun sendData(nickname: String){

        sentPackages = 0
        allPackages = byteArray.size
        try {
            val selectorManager = SelectorManager(Dispatchers.IO)
            val socket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", 5088))
            val clientId = UUID.randomUUID().toString() // генерируем уникальный идентификатор
            var packet: Datagram? = null

            while(true) {
                socket.send(Datagram(packet = ByteReadPacket(clientId.encodeToByteArray()), address = InetSocketAddress(ip, port)))
                println("packet with $clientId was sent")
                break
            }

            packet = null

            while (true)
            {
                try {
                    socket.send(Datagram(ByteReadPacket(nickname.encodeToByteArray()), InetSocketAddress(ip, port)))
                    withTimeout(3000) {
                        packet = socket.receive()
                    }
                }
                catch (e: TimeoutCancellationException) { // Exception
                    println(e.message)
                }
                if (packet != null)
                {
                    if (packet!!.packet.readString() == nickname)
                    {
                        println("Имя получателя дошло успешно")
                        break;
                    }
                }
            }

            val filename = sendingData.filename
            packet = null
            while (true)
            {
                try {
                    socket.send(Datagram(ByteReadPacket(filename.encodeToByteArray()), InetSocketAddress(ip, port)))
                    withTimeout(3000) {
                        packet = socket.receive()
                    }
                }
                catch (e: TimeoutCancellationException) { // Exception
                    println(e.message)
                }
                if (packet != null)
                {
                    if (packet!!.packet.readString() == filename)
                    {
                        println("Название дошло успешно")
                        break;
                    }
                }
            }

            packet = null
            while (true)
            {
                try {
                    socket.send(Datagram(ByteReadPacket(byteArray.size.toString().encodeToByteArray()), InetSocketAddress(ip, port)))
                    withTimeout(3000) {
                        packet = socket.receive()
                    }
                }
                catch (e: TimeoutCancellationException) { // Exception
                    println(e.message)
                }
                if (packet != null)
                {
                    if (packet!!.packet.readString() == byteArray.size.toString())
                    {
                        println("Размер (в пакетах) дошёл успешно")
                        break;
                    }
                }
            }
            packet = null
            var i = 0
            while (i < byteArray.size)
            {
                delay(90)
                try {
                    socket.send(Datagram(ByteReadPacket(byteArray[i]), InetSocketAddress(ip, port)))
                    println("$i пакет отправлен")
                    ++sentPackages
                    withTimeout(3000) {
                        packet = socket.receive()
                    }
                }
                catch (e: TimeoutCancellationException) { // Exception
                    println(e.message)
                }
                if (packet != null)
                {
                    val message = packet!!.packet.readString()
                    if (message == i.toString())
                    {
                        println("$i пакет дошёл до сервера")
                        ++i
                    }
                    else
                    {
                        ++resentPackages
                    }
                }
                packet = null
            }

            socket.close()

        } catch (e:Exception) {
            println("Ошибка: "+ e.message)
        }
    }


    suspend fun receiveData(){
        downloadingInProcess = true
        filename = "Unknown"
        byteArray = List<ByteArray>(size = 0, { byteArrayOf(3)})
        allPackages = 0
        sentPackages = 0
        resentPackages = 0
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", 5089))
        var packet: Datagram? = null
        var prevPacket: Datagram? = null

        while (true)
        {
            try {
                socket.send(
                    Datagram(
                        packet = ByteReadPacket(array = TokenStorage.getUser().toString().encodeToByteArray()),
                        address = InetSocketAddress(ip, port)
                    )
                )
                withTimeout(3000) {
                    packet = socket.receive()
                }
            }
            catch (e: TimeoutCancellationException) { // Exception
                println(e.message)
            }

            if (packet != null)
            {
                if (packet!!.packet.readString() == "okey")
                {
                    println("Адрес получателя дошёл")
                    prevPacket = packet
                    break;
                }
            }
        }

        while (true)
        {
            packet = socket.receive()
            if (packet!!.equals(prevPacket))
            {
                socket.send(
                    Datagram(
                        packet = ByteReadPacket(array = "okey".encodeToByteArray()),
                        address = packet!!.address
                    )
                )
            }
            else
            {
                filename = packet!!.packet.readString()
                socket.send(
                    Datagram(
                        packet = ByteReadPacket(array = filename.encodeToByteArray()),
                        address = packet!!.address
                    )
                )
                prevPacket = packet
                break
            }
        }
        println("File name received: $filename")

        val messageCount:String

        while (true)
        {
            packet = socket.receive()
            if (packet!!.equals(prevPacket))
            {
                socket.send(
                    Datagram(
                        packet = ByteReadPacket(array = filename.encodeToByteArray()),
                        address = packet!!.address
                    )
                )
            }
            else
            {
                messageCount = packet!!.packet.readString()
                socket.send(
                    Datagram(
                        packet = ByteReadPacket(array = messageCount.encodeToByteArray()),
                        address = packet!!.address
                    )
                )
                prevPacket = packet
                break
            }
        }

        allPackages = messageCount.toInt()
        println("messageCount: $messageCount")
        val listBytes = mutableListOf<ByteArray>()

        for (i in 0 ..<allPackages) {

            while (true)
            {
                packet = socket.receive()
                if (packet!!.equals(prevPacket))
                {
                    if (i == 0)
                    {
                        socket.send(
                            Datagram(
                                packet = ByteReadPacket(array = messageCount.encodeToByteArray()),
                                address = packet!!.address
                            )
                        )
                    }
                    else
                    {
                        socket.send(
                            Datagram(
                                packet = ByteReadPacket(
                                    array = (i-1).toString().encodeToByteArray()
                                ), address = packet!!.address
                            )
                        )
                    }
                }
                else
                {
                    val numMessage = packet!!.packet.readByteArray(4)
                    val numOfPacket = (numMessage[3].toInt() shl 24) or
                            (numMessage[2].toInt() and 0xff shl 16) or
                            (numMessage[1].toInt() and 0xff shl 8) or
                            (numMessage[0].toInt() and 0xff)

                    val dataMessage = packet!!.packet.readByteArray()

                    println("nop:$numOfPacket")
                    listBytes.add(dataMessage)
                    socket.send(
                        Datagram(
                            packet = ByteReadPacket(
                                array = numOfPacket.toString().encodeToByteArray()
                            ), address = packet!!.address
                        )
                    )
                    sentPackages++
                    prevPacket = packet
                }
            }
        }
        socket.close()
        byteArray = listBytes
        downloadingInProcess = false
        //saveFile(filename, listBytes)

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

    fun saveFile()  {
        val fileSize = byteArray.sumOf { it.size };
        val file = ByteArray(fileSize);
        var offset = 0;

        byteArray.forEach { pack ->
            System.arraycopy(pack, 0, file, offset, pack.size)
            offset += pack.size
        }

        // Вместо filename нужен путь
        //File(filename).writeBytes(file);
    }

}
