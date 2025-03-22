package com.example
import io.ktor.server.application.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.server.engine.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Serializable
data class Message(val nickname: String)

fun Application.createSocket() {
    launch {
        // Create socket connection
        while (true) {
            val socketAddress = InetSocketAddress("192.168.0.107", 2869)
            val selectorManager = SelectorManager(Dispatchers.IO)
            val serverSocket = aSocket(selectorManager).udp().bind(socketAddress)

            println("Socket bind successfully $socketAddress")
            println("Accepted $serverSocket")

            try {

                //Получаем название файла
                var packet = serverSocket.receive()
                var fileName = packet.packet.readString()
                serverSocket.send(
                    Datagram(
                        packet = ByteReadPacket(array = fileName.encodeToByteArray()),
                        address = packet.address
                    )
                )
                println("File name received: $fileName")

                // Количество (пакетов) датаграмм которые придут для резервирования массива ByteArray под нужный массив
                packet = serverSocket.receive()
                println("second packet")
                val messageCount = packet.packet.readString()
                serverSocket.send(
                    Datagram(
                        packet = ByteReadPacket(array = messageCount.encodeToByteArray()),
                        address = packet.address
                    )
                )
                println("messageCount: $messageCount")

                //Получаем пакеты с данными
                //val listBytes : List<ByteArray> = mutableListOf<ByteArray>()
                val listBytes = mutableListOf<ByteArray>()

                for (i in 0 ..<messageCount.toInt()) {
                    packet = serverSocket.receive()
                    println("packet:$packet")
                    val numMessage = packet.packet.readByteArray(4)//.toInt()
                    println("encMessage $numMessage \n")
                    val numOfPacket = (numMessage[3].toInt() shl 24) or
                            (numMessage[2].toInt() and 0xff shl 16) or
                            (numMessage[1].toInt() and 0xff shl 8) or
                            (numMessage[0].toInt() and 0xff)


                    val dataMessage = packet.packet.readByteArray()

                    println("nop:$numOfPacket")
                    listBytes.add(dataMessage)
                    serverSocket.send(
                        Datagram(
                            packet = ByteReadPacket(
                                array = numOfPacket.toString().encodeToByteArray()
                            ), address = packet.address
                        )
                    )
                }
                val message = Message("admin")
                val jsonMessage = Json.encodeToString(message)
                messageChannel.send(jsonMessage)

//                println("listFitsBytes: ${listBytes[0].size} ")
//                println("listBytesLast: ${listBytes[343].size} ")


//                 while (true)
//                {
//
//                 val message = packet.packet.readString()
//                println(message)
//                receiveChannel.writeStringUtf8("Please enter your name\n")
//                try {
//                    while (true) {
//                        val name = receiveChannel.readUTF8Line()
//                        sendChannel.writeStringUtf8("Hello, $name!\n")
//                    }
//                } catch (e: Throwable) {
//                    serverSocket.close()
//                }
//                }
            } catch (ex: Exception) {
                println("Error in socket:$ex")
            } finally {
                serverSocket.close()
                println("Socket closed")
            }

            //}
        }
    }



}



