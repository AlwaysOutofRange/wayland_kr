package wayland.protocol.xdg

import wayland.MessageBuilder
import wayland.MessageHeader
import wayland.Wayland
import wayland.protocol.Event
import wayland.protocol.WaylandObject

class ZXdgTopLevelDecoration internal constructor(
    private val wl: Wayland,
    override val objectId: Int
) : WaylandObject {
    private var currentMode: Mode = Mode.NONE

    enum class Mode(val value: Int) {
        NONE(0),           // No server-side decorations
        CLIENT_SIDE(1),    // Client-side decorations
        SERVER_SIDE(2);    // Server-side decorations

        companion object {
            fun fromInt(value: Int): Mode = entries.firstOrNull { it.value == value } ?: NONE
        }
    }

    @Event(opcode = 0)
    fun configure(mode: Int) {
        currentMode = Mode.fromInt(mode)
    }

    fun setMode(mode: Mode) {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 1, // zxdg_toplevel_decoration@set_mode
            )
        ).putInt(mode.value).build()

        wl.send(msg)
    }

    fun destroy() {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 2, // zxdg_toplevel_decoration@destroy
            )
        ).build()

        wl.send(msg)

        wl.removeObject(objectId)
    }
}