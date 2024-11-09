package wayland

import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class WaylandClient {
    private var socketFd: Int = -1
    private var msgQueue: ArrayDeque<Message> = ArrayDeque()
    private var buffer = ByteBuffer
        .allocateDirect(4096)
        .order(ByteOrder.nativeOrder())

    internal fun connect(path: String) {
        socketFd = Native.openSocket(path)
        if (socketFd < 0) {
            throw Exception("Failed to connect to wayland socket: $path")
        }
    }

    internal fun send(msg: Message, fd: Int? = null) {
        val buffer = ByteBuffer
            .allocateDirect(msg.header.size.toInt())
            .order(ByteOrder.nativeOrder())

        buffer.putInt(msg.header.objectId)
        buffer.putShort(msg.header.opcode)
        buffer.putShort(msg.header.size)
        buffer.put(msg.data)
        buffer.flip()

        val result = if (fd != null) {
            Native.sendFd(socketFd, buffer, buffer.remaining(), fd)
        } else {
            Native.write(socketFd, buffer, buffer.remaining())
        }

        if (result < 0) {
            throw Exception("Failed to send wayland message: $result")
        }
    }

    internal fun receive(): Message? {
        msgQueue.removeFirstOrNull()?.let { return it }

        val available = Native.getAvailableBytes(socketFd)
        if (available <= 0) return null

        buffer.clear()
        buffer.limit(available)

        val bytesRead = Native.readSocket(socketFd, buffer, available)
        if (bytesRead == 0) return null

        while (buffer.hasRemaining()) {
            val msg = Message.fromBuffer(buffer)
            if (msg == null) return null
            msgQueue.add(msg)
        }

        return msgQueue.removeFirstOrNull()
    }

    internal fun close() {
        if (socketFd >= 0) {
            Native.closeSocket(socketFd)
            socketFd = -1
        }
    }
}