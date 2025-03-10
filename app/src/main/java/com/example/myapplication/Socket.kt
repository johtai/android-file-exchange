package com.example.myapplication

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*

fun sendData(ip: String, port: Int){
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager).udp().bind(InetSocketAddress(ip, port))
        socket.send(Datagram(ByteReadPacket("Здесь должен быть фрагмент файла".encodeToByteArray()), InetSocketAddress(ip, port)))
    }
}

fun recieveData(ip: String, port: Int){ // вероятно, нужен контейнер, в котором будут накапливаться данные
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager).udp().bind(InetSocketAddress(ip, port))

        launch(Dispatchers.IO) {
            while (true) {
                val packet = socket.receive()
                val message = packet.packet

                socket.send(Datagram(ByteReadPacket("Здесь должна быть хэш-сумма".encodeToByteArray()), packet.address))
            }
        }
    }
}

/*
runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager).tcp().connect(ip, port)

        val receiveChannel = socket.openReadChannel()
        val sendChannel = socket.openWriteChannel(autoFlush = true)

        launch(Dispatchers.IO) {
            while (true) {
                val greeting = receiveChannel.readUTF8Line() // Получение данных
                if (greeting != null) {
                    println(greeting)  // Здесь нужно сделать сохранение файла
                } else {
                    println("Сервер закрыл соединение")
                    socket.close()
                    selectorManager.close()
                    exitProcess(0)
                }
            }
        }

        //while (true) { // нужно другое условие
            val myMessage = readln() // Передаём фрагмент файла
            sendChannel.writeStringUtf8("$myMessage\n")
        //}
    }
 */