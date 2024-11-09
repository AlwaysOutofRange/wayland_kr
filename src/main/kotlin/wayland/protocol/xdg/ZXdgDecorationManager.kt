package wayland.protocol.xdg

import wayland.MessageBuilder
import wayland.MessageHeader
import wayland.Wayland
import wayland.protocol.WaylandObject

class ZXdgDecorationManager internal constructor(
    private val wl: Wayland,
    override val objectId: Int
) : WaylandObject {
    fun getTopLevelDecoration(toplevel: Int): ZXdgTopLevelDecoration {
        val topLevelDecorationId = ++wl.nextId

        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 1, // zxdg_decoration_manager@get_toplevel_decoration
            )
        ).putInt(topLevelDecorationId).putInt(toplevel).build()

        wl.send(msg)

        val toplevelDecoration = ZXdgTopLevelDecoration(wl, topLevelDecorationId)
        wl.registerObject(toplevelDecoration)

        return toplevelDecoration
    }

    fun destroy() {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 0, // zxdg_decoration_manager@destroy
            )
        ).build()

        wl.send(msg)

        wl.removeObject(objectId)
    }
}