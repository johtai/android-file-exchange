package com.example.myapplication

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.io.readByteArray
import kotlinx.io.readString

suspend fun sendData(ip: String, port: Int, file: List<ByteArray>){

        try {
            val selectorManager = SelectorManager(Dispatchers.IO)
            val socket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", 5088))


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
                    delay(50)
                    socket.send(Datagram(ByteReadPacket(file[i]), InetSocketAddress(ip, port)))
                    println("$i пакет отправлен")
                    val packet = socket.receive()
                    val message = packet.packet.readString()
                    if (message == i.toString())
                    {
                        println("$i пакет дошёл до сервера")
                        ++sendingData.sentPackages
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
    val selectorManager = SelectorManager(Dispatchers.IO)
    val socket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", 5089))

    socket.send(
        Datagram(
            packet = ByteReadPacket(array = "Tvoy batya".encodeToByteArray()),
            address = InetSocketAddress(ip, port)
        )
    )
    var packet = socket.receive()
    var fileName = packet.packet.readString()
    socket.send(
        Datagram(
            packet = ByteReadPacket(array = fileName.encodeToByteArray()),
            address = packet.address
        )
    )
    println("File name received: $fileName")

    packet = socket.receive()
    println("second packet")
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
    sendingData.saveFile()
}