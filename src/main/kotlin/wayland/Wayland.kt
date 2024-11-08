package wayland

import wayland.protocol.WaylandObject
import wayland.protocol.wayland.WlDisplay

class Wayland {
    private var client = WaylandClient()
    private val display = WlDisplay(this)

    internal val objects = mutableMapOf<Int, WaylandObject>().apply {
        put(1, display)
    }

    internal var nextId = 1 // id 1 is reserved for wl_display

    internal fun send(msg: Message, fd: Int? = null) = client.send(msg, fd)
    internal fun receive(): Message? = client.receive()

    fun processEvents() {
        while (true) {
            val msg = receive() ?: break

            val obj = objects[msg.header.objectId] ?: break
            obj.processEvent(msg)
        }
    }

    fun connect(path: String = "/run/user/1000/wayland-0") = client.connect(path)
    fun close() = client.close()

    fun getDisplay() = display
}