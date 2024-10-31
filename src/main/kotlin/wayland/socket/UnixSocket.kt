package wayland.socket

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wayland.util.buffer.BufferPool
import wayland.util.buffer.WaylandBuffer
import wayland.util.extensions.copyTo
import wayland.util.logging.Logger
import wayland.util.result.Result
import java.net.SocketException
import wayland.util.extensions.runCatching as runC
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.channels.SocketChannel
import java.nio.file.Path

class UnixSocket private constructor(
    private val channel: SocketChannel,
    private val bufferPool: BufferPool
) : AutoCloseable {
    companion object {
        private const val DEFAULT_BUFFER_SIZE = 4096

        suspend fun connect(path: String): Result<UnixSocket> = withContext(Dispatchers.IO) {
            runC {
                val channel = SocketChannel.open(StandardProtocolFamily.UNIX)
                channel.connect(UnixDomainSocketAddress.of(Path.of(path)))
                channel.configureBlocking(false)

                UnixSocket(channel, BufferPool(DEFAULT_BUFFER_SIZE))
            }
        }
    }

    suspend fun send(data: WaylandBuffer): Result<Unit> = withContext(Dispatchers.IO) {
        runC {
            val buffer = bufferPool.acquire()

            try {
                data.copyTo(buffer)
                buffer.flip()

                while (buffer.hasRemaining()) {
                    channel.write(buffer)
                }
            } finally {
                bufferPool.release(buffer)
            }
        }
    }

    suspend fun receive(): Result<WaylandBuffer?> = withContext(Dispatchers.IO) {
        runC {
            val buffer = bufferPool.acquire()

            try {
                buffer.clear()
                when (val read = channel.read(buffer)) {
                    -1 -> throw SocketException("Stream is out of data")
                    0 -> null
                    else -> {
                        buffer.flip()
                        WaylandBuffer(read).also { waylandBuffer ->
                            buffer.copyTo(waylandBuffer)
                        }
                    }
                }
            } finally {
                bufferPool.release(buffer)
            }
        }
    }

    override fun close() {
        runC {
            channel.close()
        }.onError { e ->
            Logger.error("Error closing socket: ${e.message}")
        }
    }
}