package wayland.protocol.wayland

import wayland.MessageBuilder
import wayland.MessageHeader
import wayland.Wayland
import wayland.protocol.Event
import wayland.protocol.WaylandObject

class WlShm internal constructor(
    private val wl: Wayland,
    override val objectId: Int
) : WaylandObject {
    private val supportedFormats = mutableSetOf<Format>()

    enum class Format(val value: Int) {
        ARGB8888(0),
        XRGB8888(1),

        // Add the formats we see in proxy
        AR30(0x30335241),    // "AR30" in ASCII
        XR30(0x30335258),    // "XR30" in ASCII
        AB30(0x30334241),    // "AB30" in ASCII
        XB30(0x30334258),    // "XB30" in ASCII
        AB48(0x38344241),    // "AB48" in ASCII
        XB48(0x38344258),    // "XB48" in ASCII
        BG24(0x34324742),    // "BG24" in ASCII
        RG24(0x34324752);    // "RG24" in ASCII

        companion object {
            fun fromValue(value: Int): Format? {
                return entries.find { it.value == value }
            }

            fun isSupportedFormat(format: Int): Boolean {
                return fromValue(format) != null
            }
        }
    }

    @Event(opcode = 0)
    fun format(format: Int) {
        Format.fromValue(format)?.let {
            supportedFormats.add(it)
        }
    }

    fun createPool(fd: Int, size: Int): WlShmPool {
        val shmPoolId = ++wl.nextId

        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 0 // wl_shm@create_pool
            )
        ).putInt(shmPoolId).putInt(size).build()

        wl.send(msg, fd)

        val pool = WlShmPool(wl, shmPoolId)
        wl.registerObject(pool)

        return pool
    }

    fun release() {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 1 // wl_shm@release
            )
        ).build()

        wl.send(msg)

        wl.removeObject(objectId)
    }
}