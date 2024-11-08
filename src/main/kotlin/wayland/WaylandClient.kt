package wayland

import java.io.IOException
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentLinkedQueue

internal class WaylandClient {
    private var socket: SocketChannel = SocketChannel.open(StandardProtocolFamily.UNIX)
    private var selector: Selector = Selector.open()
    private val msgQueue = ConcurrentLinkedQueue<Message>()
    private val readBuffer = ByteBuffer.allocate(4096)

    internal fun connect(path: String) {
        socket.configureBlocking(false)
        socket.register(selector, SelectionKey.OP_READ)

        socket.connect(UnixDomainSocketAddress.of(path))

        while (!socket.finishConnect()) Thread.yield()
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

        if (fd != null) {
            val fdField = socket.javaClass.getDeclaredField("fd")
            fdField.isAccessible = true
            val fdObj = fdField.get(socket)
            val fdIntField = fdObj.javaClass.getDeclaredField("fd")
            fdIntField.isAccessible = true
            val sockFd = fdIntField.getInt(fdObj)

            val result = Native.sendFd(sockFd, buffer, buffer.remaining(), fd)
            if (result < 0) {
                throw IOException("Failed to send file descriptor")
            }
        } else {
            while (buffer.hasRemaining()) {
                socket.write(buffer)
            }
        }
    }

    internal fun receive(): Message? {
        msgQueue.poll()?.let { return it }

        if (selector.select(500) > 0) {
            readBuffer.clear()

            val bytesRead = socket.read(readBuffer)
            if (bytesRead <= 0) return null
            val readBuffer = readBuffer.slice(0, bytesRead).order(ByteOrder.nativeOrder())

            while (readBuffer.remaining() > 8) {
                val msg = Message.fromBuffer(readBuffer)
                msgQueue.add(msg)
            }
        }

        return msgQueue.poll()
    }

    internal fun close() {
        selector.close()
        socket.close()
    }
}