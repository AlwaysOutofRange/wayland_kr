package wayland.protocol.wayland

import wayland.Wayland
import wayland.protocol.Event
import wayland.protocol.WaylandObject

class WlCallback internal constructor(
    private val wl: Wayland,
    override val objectId: Int
) : WaylandObject {
    private var _done = false
    val done: Boolean get() = _done

    @Event(opcode = 0)
    fun done(data: Int) {
        _done = true
    }
}