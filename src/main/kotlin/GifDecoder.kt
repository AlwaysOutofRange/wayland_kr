import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

private class LZWDecoder(private val minCodeSize: Int) {
    private val clearCode = 1 shl minCodeSize
    private val endCode = clearCode + 1
    private var codeSize = minCodeSize + 1
    private var codeMask = (1 shl codeSize) - 1
    private val dictionary = mutableListOf<ByteArray>()

    init {
        initializeDictionary()
    }

    private fun initializeDictionary() {
        dictionary.clear()
        for (i in 0 until clearCode) {
            dictionary.add(byteArrayOf(i.toByte()))
        }
        dictionary.add(ByteArray(0))  // clear code
        dictionary.add(ByteArray(0))  // end code
        codeSize = minCodeSize + 1
        codeMask = (1 shl codeSize) - 1
    }

    fun decode(input: ByteArrayInputStream): ByteArray {
        val output = ByteArrayOutputStream()
        var bits = 0
        var bitsCount = 0

        fun readCode(): Int {
            while (bitsCount < codeSize) {
                val nextByte = input.read()
                if (nextByte == -1) return -1
                bits = bits or (nextByte shl bitsCount)
                bitsCount += 8
            }

            val code = bits and codeMask
            bits = bits shr codeSize
            bitsCount -= codeSize
            return code
        }

        var oldCode = -1
        var code = readCode()
        if (code == -1) return ByteArray(0)

        while (true) {
            when {
                code == clearCode -> {
                    initializeDictionary()
                    code = readCode()
                    if (code == -1 || code == endCode) break
                    output.write(dictionary[code])
                    oldCode = code
                }

                code == endCode -> break
                else -> {
                    val entry = when {
                        code < dictionary.size -> dictionary[code]
                        code == dictionary.size && oldCode != -1 -> {
                            val prev = dictionary[oldCode]
                            ByteArray(prev.size + 1).also {
                                System.arraycopy(prev, 0, it, 0, prev.size)
                                it[prev.size] = prev[0]
                            }
                        }

                        else -> continue
                    }

                    output.write(entry)

                    if (oldCode != -1 && dictionary.size < 4096) {
                        val prev = dictionary[oldCode]
                        val newEntry = ByteArray(prev.size + 1)
                        System.arraycopy(prev, 0, newEntry, 0, prev.size)
                        newEntry[prev.size] = entry[0]
                        dictionary.add(newEntry)

                        if (dictionary.size and codeMask == 0 && dictionary.size < 4096) {
                            codeSize++
                            codeMask = (1 shl codeSize) - 1
                        }
                    }
                    oldCode = code
                }
            }
            code = readCode()
            if (code == -1) break
        }

        return output.toByteArray()
    }
}

class GifDecoder {
    data class Frame(
        val width: Int,
        val height: Int,
        val pixels: ByteBuffer,
        val transparentIndex: Int = -1,
        val delayMs: Int = 100
    )

    fun decode(gifData: ByteArray): List<Frame> {
        val frames = mutableListOf<Frame>()
        val input = ByteArrayInputStream(gifData)

        // Skip GIF Header
        input.skip(6)

        // Read logical screen size
        input.read() or (input.read() shl 8)
        input.read() or (input.read() shl 8)
        val packed = input.read()
        input.skip(2)

        // Read Global Color Table if present
        val globalColorTable = if (packed and 0x80 != 0) {
            val tableSize = 3 * (1 shl ((packed and 0x07) + 1))
            ByteArray(tableSize).also { input.read(it) }
        } else null

        var transparentIndex = -1
        var delayTime = 100  // Default delay

        while (true) {
            when (input.read()) {
                0x21 -> {  // Extension Introducer
                    when (input.read()) {
                        0xF9 -> {  // Graphics Control Extension
                            var blockSize = input.read()
                            if (blockSize == 4) {
                                val flags = input.read()
                                // Read delay time (in 1/100th of a second)
                                delayTime = (input.read() or (input.read() shl 8)) * 10 // Convert to milliseconds
                                transparentIndex = if (flags and 0x01 != 0) input.read() else -1
                                input.read()  // Block terminator
                            }
                        }

                        else -> {
                            var blockSize = input.read()
                            while (blockSize > 0) {
                                input.skip(blockSize.toLong())
                                blockSize = input.read()
                            }
                        }
                    }
                }

                0x2C -> {  // Image Separator
                    input.skip(4)
                    val frameWidth = input.read() or (input.read() shl 8)
                    val frameHeight = input.read() or (input.read() shl 8)
                    val imagePacked = input.read()

                    val localColorTable = if (imagePacked and 0x80 != 0) {
                        val tableSize = 3 * (1 shl ((imagePacked and 0x07) + 1))
                        ByteArray(tableSize).also { input.read(it) }
                    } else null

                    val colorTable = localColorTable ?: globalColorTable ?: ByteArray(0)

                    val lzwMinCodeSize = input.read()

                    val imageDataStream = ByteArrayOutputStream()
                    var blockSize = input.read()
                    while (blockSize > 0) {
                        val block = ByteArray(blockSize)
                        input.read(block)
                        imageDataStream.write(block)
                        blockSize = input.read()
                    }

                    val lzwDecoder = LZWDecoder(lzwMinCodeSize)
                    val indexedPixels = lzwDecoder.decode(ByteArrayInputStream(imageDataStream.toByteArray()))

                    val stride = frameWidth * 4
                    val rgbaBuffer = ByteBuffer.allocateDirect(frameHeight * stride)

                    for (y in 0 until frameHeight) {
                        for (x in 0 until frameWidth) {
                            val pixelIndex = y * frameWidth + x
                            val bufferOffset = (y * stride) + (x * 4)

                            if (pixelIndex < indexedPixels.size && colorTable.isNotEmpty()) {
                                val colorIndex = indexedPixels[pixelIndex].toInt() and 0xFF
                                val index = colorIndex * 3

                                if (colorIndex == transparentIndex) {
                                    rgbaBuffer.put(bufferOffset + 0, 0)
                                    rgbaBuffer.put(bufferOffset + 1, 0)
                                    rgbaBuffer.put(bufferOffset + 2, 0)
                                    rgbaBuffer.put(bufferOffset + 3, 0)
                                } else if (index + 2 < colorTable.size) {
                                    rgbaBuffer.put(bufferOffset + 0, colorTable[index])
                                    rgbaBuffer.put(bufferOffset + 1, colorTable[index + 1])
                                    rgbaBuffer.put(bufferOffset + 2, colorTable[index + 2])
                                    rgbaBuffer.put(bufferOffset + 3, 0xFF.toByte())
                                }
                            }
                        }
                    }
                    frames.add(Frame(frameWidth, frameHeight, rgbaBuffer, transparentIndex, delayTime))
                }

                0x3B, -1 -> break  // Trailer or EOF
            }
        }

        return frames
    }
}