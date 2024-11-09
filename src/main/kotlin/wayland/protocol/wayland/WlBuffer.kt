package wayland.protocol.wayland

import wayland.MessageBuilder
import wayland.MessageHeader
import wayland.Wayland
import wayland.protocol.Event
import wayland.protocol.WaylandObject

class WlBuffer internal constructor(
    private val wl: Wayland,
    override val objectId: Int
) : WaylandObject {
    @Event(opcode = 0)
    fun release() {
        // Buffer is released and can be reused
    }

    fun destroy() {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 0 // wl_buffer@destroy
            )
        ).build()

        wl.send(msg)

        wl.removeObject(objectId)
    }
}