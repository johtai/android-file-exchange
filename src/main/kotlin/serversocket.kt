package com.example
import io.ktor.server.application.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.server.engine.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.readByteArray
import kotlinx.io.readString

fun Application.createSocket() {
    launch {
        val socketAddress = InetSocketAddress("192.168.0.107", 2869)
        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).udp().bind(socketAddress)

        println("Socket bind successfully $socketAddress")

        println("Accepted $serverSocket")

        try {
            var packet = serverSocket.receive()
            var message = packet.packet.readString()
            serverSocket.send(Datagram(packet = ByteReadPacket(array = message.encodeToByteArray()), address = packet.address))
            println(message)
            //Количество датаграмм которые придут для резервирования массива ByteArray под нужный массив
            packet = serverSocket.receive()
            println("second packet")
            val messageCount = packet.packet.readString()
            serverSocket.send(Datagram(packet = ByteReadPacket(array = messageCount.encodeToByteArray()), address = packet.address))
            println(messageCount)


            //val listBytes : List<ByteArray> = mutableListOf<ByteArray>()
            //val listBytes = mutableListOf<ByteArray>()

//            for(i in 0..< messageCount.toInt())
//            {
//                packet = serverSocket.receive()
//                println("packet:$packet")
//                val encMessage = packet.packet.readByteArray()
//
//                val numOfPacket = (encMessage[3].toInt() shl 24) or
//                        (encMessage[2].toInt() and 0xff shl 16) or
//                        (encMessage[1].toInt() and 0xff shl 8) or
//                        (encMessage[0].toInt() and 0xff)
//                println(numOfPacket.toString())
//                // listBytes.add(message.encodeToByteArray())
//                serverSocket.send(Datagram(packet = ByteReadPacket(array = numOfPacket.toString().encodeToByteArray()), address = packet.address))
//            }



            //while (true)
            //{

                // val message = packet.packet.readString()
                //println(message)
//                receiveChannel.writeStringUtf8("Please enter your name\n")
//                try {
//                    while (true) {
//                        val name = receiveChannel.readUTF8Line()
//                        sendChannel.writeStringUtf8("Hello, $name!\n")
//                    }
//                } catch (e: Throwable) {
//                    serverSocket.close()
//                }
            //}
        }catch (ex: Exception){
            println("Error in socket:$ex")
        }
        finally {
            serverSocket.close()
            println("Socket closed")
        }

        //}
    }



}



