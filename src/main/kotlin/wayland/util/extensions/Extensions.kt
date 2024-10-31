package wayland.util.extensions

import wayland.util.buffer.WaylandBuffer
import wayland.util.result.Result
import java.nio.ByteBuffer

fun WaylandBuffer.copyTo(target: ByteBuffer) {
    val bytes = this.toByteArray()
    target.put(bytes)
}

fun WaylandBuffer.write(bytes: ByteArray) {
    bytes.forEach { b ->
        putByte(b)
    }
}

fun ByteBuffer.copyTo(target: WaylandBuffer) {
    val bytes = ByteArray(remaining())
    get(bytes)
    target.write(bytes)
}

inline fun <T> runCatching(block: () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: Exception) {
    Result.Error(e)
}