package wayland

import java.nio.ByteBuffer
import java.nio.file.Paths

internal class Native {
    companion object {
        init {
            // Hardcoded for now
            val currentPath = Paths.get("").toAbsolutePath().toString()
            System.load("$currentPath/src/main/resources/native/libwaylandkt.so")
        }

        @JvmStatic
        external fun openSocket(path: String): Int

        @JvmStatic
        external fun closeSocket(fd: Int): Int

        @JvmStatic
        external fun write(fd: Int, buffer: ByteBuffer, length: Int): Int

        @JvmStatic
        external fun sendFd(sockFd: Int, data: ByteBuffer, dataLength: Int, fd: Int): Int

        @JvmStatic
        external fun getAvailableBytes(fd: Int): Int

        @JvmStatic
        external fun readSocket(fd: Int, buffer: ByteBuffer, length: Int): Int
    }
}