package com.example.myapplication

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*

fun sendData(ip: String, port: Int, file: List<ByteArray>){
    runBlocking {
        try {
            val selectorManager = SelectorManager(Dispatchers.IO)
            val socket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", 5088))

            launch(Dispatchers.IO) {
                socket.send(Datagram(ByteReadPacket("1.jpg".encodeToByteArray()), InetSocketAddress(ip, port)))
                socket.send(Datagram(ByteReadPacket(file.size.toString().encodeToByteArray()), InetSocketAddress(ip, port)))
            }
        } catch (e:Exception) {
            println("Error: "+ e.message)
        }


        var i = 0

        /*

        launch(Dispatchers.IO) {
            while (true) {
                socket.send(Datagram(ByteReadPacket(file[i]), InetSocketAddress(ip, port))) // Нужно поделить файл так, чтобы в дейтаграмме было место для номера
                do {
                    val packet = socket.receive()
                    val message = packet.packet
                    if (true) // если пришёл в ответ целый пакет с номером, то ок (потом будет асинхронность)
                    {
                        ++i;
                        break;
                    }
                    else
                    {
                        socket.send(Datagram(ByteReadPacket(file[i]), InetSocketAddress(ip, port)))
                    }
                } while(true)
            }
        }
         */
    }
}

fun recieveData(ip: String, port: Int, file: List<ByteArray>){
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager).udp().bind(InetSocketAddress(ip, port))

        launch(Dispatchers.IO) {
            while (true) {
                val packet = socket.receive()
                val message = packet.packet
                if (true) // если пришёл целый пакет с номером, то ок (потом будет асинхронность)
                {
                    // добавление в файл
                    socket.send(Datagram(ByteReadPacket("Здесь должен быть номер".encodeToByteArray()), InetSocketAddress(ip, port)))
                }
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