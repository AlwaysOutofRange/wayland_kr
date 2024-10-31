package wayland.socket

import wayland.util.buffer.WaylandBuffer
import java.nio.ByteBuffer

class Message(
    val objectId: Int,
    val opcode: Int,
    val data: ByteBuffer
) {
    fun serialize(): WaylandBuffer {
        val size = 8 + data.limit()
        return WaylandBuffer(size).apply {
            putInt(objectId)
            putShort(opcode.toShort())
            putShort(size.toShort())
            putBytes(data)
        }
    }

    companion object {
        fun parse(buffer: WaylandBuffer): Message {
            buffer.flip()
            val objectId = buffer.getInt()
            val opcode = buffer.getShort()
            val size = buffer.getShort()

            return Message(objectId, opcode.toInt(), ByteBuffer.wrap(buffer.getBytes(size.toInt())))
        }
    }
}