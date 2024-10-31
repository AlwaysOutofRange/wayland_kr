package wayland.socket

import wayland.util.collections.AsyncQueue
import wayland.util.logging.Logger
import wayland.util.result.Result
import wayland.util.result.map

typealias MessageQueue = AsyncQueue<Message>

class Connection private constructor(
    private val socket: UnixSocket,
    private val messageQueue: MessageQueue
) : AutoCloseable {
    companion object {
        private const val DEFAULT_SOCKET_PATH = "/run/user/1000/wayland-0"

        suspend fun connect(display: String? = null): Result<Connection> {
            val socketPath = display?.let { "/run/user/1000/wayland-$it" } ?: DEFAULT_SOCKET_PATH

            return UnixSocket.connect(socketPath).map { s ->
                Connection(s, AsyncQueue())
            }
        }
    }

    suspend fun send(message: Message): Result<Unit> {
        return message.serialize().let { buffer ->
            socket.send(buffer)
        }
    }

    suspend fun receive(handler: (Message) -> Unit) {
        while (true) {
            val result = socket.receive()

            result.onSuccess { msg ->
                if (msg == null) return
                handler(Message.parse(msg))
            }.onError { err ->
                Logger.error("Error receiving message: $err")
                return
            }
        }
    }

    override fun close() {
        socket.close()
    }
}