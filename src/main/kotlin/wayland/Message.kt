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

    // For now only int supported
    fun readArray(): Array<Int> {
        val size = readInt()
        if (size <= 0) return emptyArray()

        val numInts = size / 4
        val results = Array<Int>(numInts) { readInt() }

        val padding = (4 - (size % 4)) % 4
        data.position(data.position() + padding)

        return results
    }

    companion object {
        fun fromBuffer(buffer: ByteBuffer): Message? {
            val objectId = buffer.getInt()
            val opcode = buffer.getShort()
            val size = buffer.getShort()

            if (size - 8 > buffer.remaining()) return null

            val data = buffer.slice().limit(size - 8).order(ByteOrder.nativeOrder())
            buffer.position(buffer.position() + size - 8)

            return Message(MessageHeader(objectId, opcode, size), data)
        }
    }
}