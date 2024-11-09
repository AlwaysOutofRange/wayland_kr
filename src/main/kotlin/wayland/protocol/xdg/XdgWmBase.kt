package wayland.protocol.xdg

import wayland.MessageBuilder
import wayland.MessageHeader
import wayland.Wayland
import wayland.protocol.Event
import wayland.protocol.WaylandObject
import wayland.protocol.wayland.WlSurface

class XdgWmBase internal constructor(
    private val wl: Wayland,
    override val objectId: Int
) : WaylandObject {
    @Event(opcode = 0)
    fun ping(serial: Int) {
        pong(serial)
    }

    fun getXdgSurface(surface: WlSurface): XdgSurface {
        val xdgSurfaceId = ++wl.nextId
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 2, // xdg_wm_base@get_xdg_surface
            )
        ).putInt(xdgSurfaceId).putInt(surface.objectId).build()

        wl.send(msg)

        val xdgSurface = XdgSurface(wl, xdgSurfaceId)
        wl.registerObject(xdgSurface)

        return xdgSurface
    }

    private fun pong(serial: Int) {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 3, // xdg_wm_base@pong
            )
        ).putInt(serial).build()

        wl.send(msg)
    }

    fun destroy() {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 1, // xdg_wm_base@destroy
            )
        ).build()

        wl.send(msg)

        wl.removeObject(objectId)
    }
}