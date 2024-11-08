package wayland

import java.nio.ByteBuffer
import java.nio.ByteOrder

class MessageBuilder (
    private val header: MessageHeader
) {
    private val buffer = ByteBuffer.allocate(1024).order(ByteOrder.nativeOrder())

    init {
        this.buffer.putInt(header.objectId)
        this.buffer.putShort(header.opcode)
        this.buffer.putShort(0) // Placeholder for message length
    }

    fun putInt(value: Int): MessageBuilder {
        this.buffer.putInt(value)
        return this
    }

    fun putString(value: String): MessageBuilder {
        val bytes = value.toByteArray(Charsets.UTF_8)
        val length = bytes.size + 1
        val padding = (4 - (length % 4)) % 4

        this.buffer.putInt(length)
        this.buffer.put(bytes)
        this.buffer.put(0)

        repeat(padding) { this.buffer.put(0) }

        return this
    }

    fun build(): Message {
        val messageLength = this.buffer.position()
        this.buffer.putShort(6, messageLength.toShort())
        this.buffer.flip()

        val payloadSize = messageLength - 8
        header.size = messageLength.toShort()

        val payloadBuffer = ByteBuffer.allocate(payloadSize).order(ByteOrder.nativeOrder())
        this.buffer.position(8)
        payloadBuffer.put(this.buffer.slice().limit(payloadSize))
        payloadBuffer.flip()

        return Message(header, payloadBuffer)
    }
}