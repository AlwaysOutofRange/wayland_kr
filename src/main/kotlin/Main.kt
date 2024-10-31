import kotlinx.coroutines.*
import wayland.socket.ConnectionManager
import wayland.socket.Message
import wayland.util.logging.Logger
import java.nio.ByteBuffer

fun main(): Unit =
    runBlocking {
        val connectionManager = ConnectionManager()

        try {
            Logger.info("Connecting to Wayland display...")

            connectionManager.connectToDisplay()
                .onSuccess { con ->
                    Logger.info("Connected to Wayland display")

                    val message = Message(
                        objectId = 1,
                        opcode = 1,
                        data = ByteBuffer.allocate(4).apply {
                            putInt(2)
                        }
                    )

                    con.send(message)
                        .onSuccess {
                            Logger.info("Message sent")
                        }
                        .onError { err ->
                            Logger.error("Error sending message: $err")
                        }

                    delay(3)

                    con.receive { msg ->
                        val objectId = msg.objectId
                        val opcode = msg.opcode
                        val data = msg.data.array().decodeToString()
                        Logger.info("Received message: $objectId - $opcode - $data")
                    }

                }
                .onError { err ->
                    Logger.error("Error connecting to Wayland display: ${err.message}")
                }
        } catch (e: Exception) {
            Logger.error("Error connecting to Wayland display: ${e.message}")
        } finally {
            connectionManager.close()
            Logger.info("Disconnected from Wayland display")
        }
    }