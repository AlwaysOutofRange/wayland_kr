package wayland.protocol.wayland

import wayland.MessageBuilder
import wayland.MessageHeader
import wayland.Wayland
import wayland.protocol.WaylandObject

class WlShmPool internal constructor(
    private val wl: Wayland,
    override val objectId: Int
) : WaylandObject {
    fun createBuffer(offset: Int, width: Int, height: Int, stride: Int, format: WlShm.Format): WlBuffer {
        if (!WlShm.Format.isSupportedFormat(format.value)) {
            error("Unsupported format: $format")
        }

        val bufferId = ++wl.nextId

        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 0 // wl_shm_pool@create_buffer
            )
        )
            .putInt(bufferId)
            .putInt(offset)
            .putInt(width)
            .putInt(height)
            .putInt(stride)
            .putInt(format.value)
            .build()

        wl.send(msg)

        val buffer = WlBuffer(wl, bufferId)
        wl.registerObject(buffer)

        return buffer
    }

    fun destroy() {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 1 // wl_shm_pool@destroy
            )
        ).build()

        wl.send(msg)

        wl.removeObject(objectId)
    }
}