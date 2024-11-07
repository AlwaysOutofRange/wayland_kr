package wayland

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class MessageHeader(
    val objectId: Int,
    val opcode: Short,
    var size: Short = 8
)

data class Message(
    val header: MessageHeader,
    val data: ByteBuffer
) {
    fun readInt(): Int {
        return data.getInt()
    }

    fun readString(): String {
        var messageLength = readInt()
        var message = ByteArray(messageLength).apply {
            data.get(this)
            data.position(data.position() + (4 - (messageLength % 4)) % 4)
        }.decodeToString().trimEnd('\u0000')

        return message
    }

    companion object {
        fun fromBuffer(buffer: ByteBuffer): Message? {
            val objectId = buffer.getInt()
            val opcode = buffer.getShort()
            val size = buffer.getShort()

            if (size - 8 > buffer.remaining()) return null

            val data = buffer.slice().limit(size - 8).order(ByteOrder.nativeOrder())
            buffer.position(buffer.position() + data.limit())

            return Message(MessageHeader(objectId, opcode, size), data)
        }
    }
}