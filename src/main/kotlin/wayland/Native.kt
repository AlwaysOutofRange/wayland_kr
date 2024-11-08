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
        external fun sendFd(sockFd: Int, data: ByteBuffer, dataLength: Int, fd: Int): Int
    }
}