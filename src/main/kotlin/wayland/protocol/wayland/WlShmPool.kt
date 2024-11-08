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
        val bufferId = ++wl.nextId

        println("""
            Creating buffer with:
            bufferId: $bufferId
            offset: $offset
            width: $width
            height: $height
            stride: $stride
            format: ${format.value}
        """.trimIndent())

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
        wl.objects.put(bufferId, buffer)

        return buffer
    }
}