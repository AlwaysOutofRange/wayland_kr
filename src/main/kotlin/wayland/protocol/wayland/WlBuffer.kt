package wayland.protocol.wayland

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
}