package wayland.util.buffer

import java.nio.ByteBuffer

class WaylandBuffer(size: Int) {
    private val buffer = ByteArray(size)
    private var position = 0
    private var size = 0

    fun putByte(value: Byte) {
        buffer[position++] = value
        size++
    }

    fun putBytes(bytes: ByteBuffer) {
        // reverse the bytes
        for (i in 0 until bytes.limit()) {
            putByte(bytes[bytes.limit() - 1 - i])
        }
    }

    fun getBytes(size: Int): ByteArray {
        val bytes = buffer.copyOfRange(position, position + size - 8)
        position += size

        // Skip padding
        position += (4 - (size % 4)) % 4

        return bytes
    }

    fun putInt(value: Int) {
        buffer[position++] = value.toByte()
        buffer[position++] = (value shr 8).toByte()
        buffer[position++] = (value shr 16).toByte()
        buffer[position++] = (value shr 24).toByte()

        size += 4
    }

    fun getInt(): Int {
        val value = (buffer[position].toInt() and 0xFF) or
                ((buffer[position + 1].toInt() and 0xFF) shl 8) or
                ((buffer[position + 2].toInt() and 0xFF) shl 16) or
                ((buffer[position + 3].toInt() and 0xFF) shl 24)
        position += 4
        return value
    }

    fun putShort(value: Short) {
        buffer[position++] = value.toByte()
        buffer[position++] = (value.toInt() shr 8).toByte()
        size += 2
    }

    fun getShort(): Short {
        val value = (buffer[position].toInt() and 0xFF) or
                ((buffer[position + 1].toInt() and 0xFF) shl 8)
        position += 2
        return value.toShort()
    }

    fun putString(str: String) {
        val bytes = str.toByteArray()
        putInt(bytes.size)
        bytes.copyInto(buffer, position)
        position += bytes.size

        // Adding padding to maintain 4 byte alignment
        val padding = (4 - (position % 4)) % 4
        repeat(padding) { buffer[position++] = 0 }

        size += bytes.size
    }

    fun getString(length: Int): String {
        val bytes = buffer.copyOfRange(position, position + length - 8)
        position += length

        // Skip padding
        position += (4 - (position % 4)) % 4
        return bytes.toString(Charsets.UTF_8)
    }

    fun toByteArray(): ByteArray = buffer.copyOf(position)

    fun remaining(): Int = buffer.size - position

    fun flip() {
        size = position
        position = 0
    }
}