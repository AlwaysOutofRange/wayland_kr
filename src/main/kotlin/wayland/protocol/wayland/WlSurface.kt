package wayland.protocol.wayland

import wayland.MessageBuilder
import wayland.MessageHeader
import wayland.Wayland
import wayland.protocol.WaylandObject

class WlSurface internal constructor(
    private val wl: Wayland,
    override val objectId: Int
) : WaylandObject {
    fun attach(buffer: WlBuffer?, x: Int, y: Int) {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 1 // wl_surface@attach
            )
        ).putInt(buffer?.objectId ?: 0).putInt(x).putInt(y).build()

        wl.send(msg)
    }

    fun damage(x: Int, y: Int, width: Int, height: Int) {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 2 // wl_surface@damage
            )
        ).putInt(x).putInt(y).putInt(width).putInt(height).build()

        wl.send(msg)
    }

    fun commit() {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 6 // wl_surface@commit
            )
        ).build()

        wl.send(msg)
    }

    fun frame() {
        val callbackId = ++wl.nextId

        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 3, // wl_surface@frame
            ),
        ).putInt(callbackId).build()

        wl.send(msg)
        --wl.nextId
    }
}