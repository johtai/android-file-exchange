package com.example
import io.ktor.server.application.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.withTimeoutOrNull
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.readString
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Serializable
data class Message(val nickname: String)

data class ClientState(val address: SocketAddress, var recipientsName: String? = null,  var fileName: String? = null, var messageCount: Int? = null, var receivedPackets: MutableList<ByteArray> = mutableListOf())

object ClientManager{
    val clients = mutableMapOf<String, ClientState>()
}

fun Application.createSocket()
{

    launch {
        // Create socket connection
        val socketAddress = InetSocketAddress("0.0.0.0", 2869)
        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).udp().bind(socketAddress)

        println("Socket bind successfully $socketAddress")

        while (true)
        {
            try {
                //Принимает новое подключение
                val clientPacket = serverSocket.receive()
                val clientIdData = clientPacket.packet.readString()
                val clientAddress = clientPacket.address

                val clientId =  ClientManager.clients.entries.find{it.value.address == clientAddress}?.key
                if (clientId == null)
                {
                    // Новый клиент
                    ClientManager.clients[clientIdData] = ClientState(clientAddress)
                    println("New client connected: ${ClientManager.clients[clientIdData]} from $clientAddress")
                }
                else{
                    // Существует клиент
                    val clientState = ClientManager.clients[clientId]!!
                    handleClient(clientId, clientState, clientIdData, serverSocket)
                }

            } catch (ex: Exception) {
                println("Error accepting client: ${ex.message}")
            }
        }
    }
}


fun toi(ba:ByteArray):Int
{
     return (ba[3].toInt() shl 24) or
            (ba[2].toInt() and 0xff shl 16) or
            (ba[1].toInt() and 0xff shl 8) or
            (ba[0].toInt() and 0xff)
}

suspend fun handleClient(clientId: String, clientState: ClientState, data: String, serverSocket: BoundDatagramSocket) {
    try {
        when {
            clientState.recipientsName == null ->{
                //Первый пакет - имя получателя
                clientState.recipientsName = data
                println("recipient's name: $data")
                serverSocket.send(
                    Datagram(
                        packet = ByteReadPacket(array = data.encodeToByteArray()),
                        address = clientState.address
                    )
                )

            }

            clientState.fileName == null -> {
                // Второй пакет — название файла
                clientState.fileName = data
                println("Client $clientId: File name received: ${clientState.fileName}")
                serverSocket.send(
                    Datagram(
                        packet = ByteReadPacket(array = data.encodeToByteArray()),
                        address = clientState.address
                    )
                )


            }
            clientState.messageCount == null -> {
                // третий пакет — количество пакетов
                clientState.messageCount = data.toInt()
                println("Client $clientId: Message count received: ${clientState.messageCount}")
                serverSocket.send(
                    Datagram(
                        packet = ByteReadPacket(array = data.encodeToByteArray()),
                        address = clientState.address
                    )
                )
            }
            else -> {
                // Последующие пакеты — данные
                val numMessage = data.toByteArray()
                if(numMessage.size < 4){
                    println("Client $clientId: Error - Invalid packet size")
                    return
                }
                val numOfPacket = (numMessage[3].toInt() shl 24) or
                        (numMessage[2].toInt() and 0xff shl 16) or
                        (numMessage[1].toInt() and 0xff shl 8) or
                        (numMessage[0].toInt() and 0xff)

                val dataMessage = data.toByteArray()//.drop(4).toByteArray()
                clientState.receivedPackets.add(dataMessage)
                println("Client $clientId: Packet $numOfPacket received")
                serverSocket.send(
                    Datagram(
                        packet = ByteReadPacket(array = numOfPacket.toString().encodeToByteArray()),
                        address = clientState.address
                    )
                )

                println("size Recived packets: ${clientState.receivedPackets.size} ")
                println("messageCount: ${clientState.messageCount} ")

                // ---------Проверяем, все ли пакеты получены-----------
                if (clientState.receivedPackets.size == clientState.messageCount) {
                    println("Client $clientId: All packets received")
                    // Обработка завершена
                    //ClientManager.clients.remove(clientId)

                    val message = Message(clientState.recipientsName.toString())
                    messageChannel.send(Json.encodeToString(message))
                    println("After message channel")

                    //Получение пакета адреса
                    // var packAddress: SocketAddress

                    var pack = serverSocket.receive()
                    println(" packAddressDestination Recived: ${pack.packet.readString()}")
                    var packAddress = pack.address
                    serverSocket.send(Datagram(packet = ByteReadPacket(
                        "okey".encodeToByteArray()), address = pack.address))


                    while (true) {

                        serverSocket.send(
                            Datagram(
                                ByteReadPacket(clientState.fileName!!.encodeToByteArray()),
                                packAddress
                            )
                        )
                        // println("Пакет в цикле отправлен")
                        val pack = withTimeoutOrNull(3000){
                            serverSocket.receive()
                        }
                        if(pack == null){
                            println("Таймаут подтверждения для пакета с названием. Повторная отправка...")
                            continue
                        }
                        if (pack.packet.readString() == clientState.fileName!!) {
                            println("Название дошло успешно")
                            break
                        }

                    }

                    while (true) {
                        serverSocket.send(
                            Datagram(
                                ByteReadPacket(
                                    clientState.messageCount.toString().encodeToByteArray()
                                ), packAddress
                            )
                        )
                        val packet = withTimeoutOrNull(3000)
                        {
                            serverSocket.receive()
                        }
                        if(packet == null){
                            println("Таймаут подтверждения для размера пакета. Повторная отправка...")
                            continue
                        }
                        if (packet.packet.readString() == clientState.messageCount.toString()) {
                            println("Размер (в пакетах) дошёл успешно")
                            break
                        }
                    }

                    val bufferNum = ByteArray(4)
                    var i = 0
                    while (i <clientState.messageCount!!) {
                        delay(50)
                        for (j in 0..3) bufferNum[j] = (i shr (j * 8)).toByte()
                        //serverSocket.send(Datagram(ByteReadPacket(clientState.receivedPackets[i]), packAddress))
                        serverSocket.send(Datagram(ByteReadPacket(bufferNum.copyOf(4) + clientState.receivedPackets[i]), packAddress))
                        println("$i пакет отправлен")


                        val packet = withTimeoutOrNull(3000){
                            serverSocket.receive()
                        }
                        if(packet == null){
                            println("Таймаут подтверждения для пакета $i, повторная отправка...")
                            continue
                        }
                        val message = packet.packet.readString()
                        if (message == i.toString()) {
                            ++i
                            println("$i пакет дошёл до сервера")
                            //++sendingData.sendingPackages
                        } else {
                            println("Повторно отправил пакет")
                            serverSocket.send(Datagram(ByteReadPacket(clientState.receivedPackets[i]), packAddress))
                        }
                    }
                    //serverSocket.close()
                    println("End of sending packets.")
                    ClientManager.clients.remove(clientId)
                    println(ClientManager.clients.size.toString())
                }

            }
        }
    } catch (ex: Exception) {
        println("Client $clientId: Error handling packet: ${ex.message}")
        ex.printStackTrace()
        ClientManager.clients.remove(clientId) // Удаляем клиента в случае ошибки
    }
}
