package wayland.protocol.xdg

import wayland.MessageBuilder
import wayland.MessageHeader
import wayland.Wayland
import wayland.protocol.Event
import wayland.protocol.WaylandObject

class XdgSurface internal constructor(
    private val wl: Wayland,
    override val objectId: Int
) : WaylandObject {
    fun getToplevel(): XdgTopLevel {
        val toplevelId = ++wl.nextId

        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 1, // xdg_surface@get_toplevel
            ),
        ).putInt(toplevelId).build()

        wl.send(msg)

        val toplevel = XdgTopLevel(wl, toplevelId)
        wl.objects.put(toplevelId, toplevel)

        return toplevel
    }

    @Event(opcode = 0)
    fun configure(serial: Int) {
        // Must acknowledge configure
        ackConfigure(serial)
    }

    private fun ackConfigure(serial: Int) {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 4, // xdg_surface@ack_configure
            ),
        ).putInt(serial).build()

        wl.send(msg)
    }
}