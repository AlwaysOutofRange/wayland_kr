package wayland.protocol.xdg

import wayland.MessageBuilder
import wayland.MessageHeader
import wayland.Wayland
import wayland.protocol.Event
import wayland.protocol.WaylandObject

class XdgTopLevel internal constructor(
    private val wl: Wayland,
    override val objectId: Int
) : WaylandObject {
    fun setTitle(title: String) {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 2, // xdg_toplevel@set_title
            ),
        ).putString(title).build()

        wl.send(msg)
    }

    fun setAppId(appId: String) {
        val appIdBytes = appId.toByteArray()
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 3, // xdg_toplevel@set_app_id
            ),
        ).putString(appId).build()

        wl.send(msg)
    }

    @Event(opcode = 0)
    fun configure(width: Int, height: Int, states: Int) {
        // Handle window configuration
    }

    @Event(opcode = 1)
    fun close() {
        // Handle window close request
    }
}