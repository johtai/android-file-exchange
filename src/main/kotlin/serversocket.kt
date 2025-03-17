package com.example
import io.ktor.server.application.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.readString

suspend fun createsocket(){

    runBlocking {
        val socketAddress = InetSocketAddress("127.0.0.1", 2868)
        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).udp().bind(socketAddress)

        println("Socket bind successfully $socketAddress")
//        val receiveChannel = socket.openReadChannel()
//        val sendChannel = socket.openWriteChannel(autoFlush = true)

        while (true) {
            println("Accepted $serverSocket")
            launch {
                val packet = serverSocket.receive()
                val message = packet.packet.readString()
                println(message)
//                receiveChannel.writeStringUtf8("Please enter your name\n")
//                try {
//                    while (true) {
//                        val name = receiveChannel.readUTF8Line()
//                        sendChannel.writeStringUtf8("Hello, $name!\n")
//                    }
//                } catch (e: Throwable) {
//                    serverSocket.close()
//                }
            }
        }


    }

}


