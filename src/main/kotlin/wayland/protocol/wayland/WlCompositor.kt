package wayland.protocol.wayland

import wayland.MessageBuilder
import wayland.MessageHeader
import wayland.Wayland
import wayland.protocol.WaylandObject

class WlCompositor internal constructor(
    private val wl: Wayland,
    override val objectId: Int
) : WaylandObject {
    fun createSurface(): WlSurface {
        val surfaceId = ++wl.nextId

        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 0 // wl_compositor@create_surface
            )
        ).putInt(surfaceId).build()

        wl.send(msg)

        val surface = WlSurface(wl, surfaceId)
        wl.registerObject(surface)

        return surface
    }
}