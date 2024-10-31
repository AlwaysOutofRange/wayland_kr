package wayland.util.buffer

import wayland.util.collections.ObjectPool
import java.nio.ByteBuffer

class BufferPool(
    private val bufferSize: Int,
    private val direct: Boolean = true
) {
    private val pool = ObjectPool(
        factory = {
            if (direct) ByteBuffer.allocateDirect(bufferSize)
            else ByteBuffer.allocate(bufferSize)
        },
        reset = { it.clear() }
    )

    fun acquire(): ByteBuffer = pool.acquire()

    fun release(buffer: ByteBuffer) {
        buffer.clear()
        pool.release(buffer)
    }
}