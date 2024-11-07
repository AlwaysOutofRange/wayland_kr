package wayland

import wayland.protocol.wayland.WlDisplay

class Wayland {
    private var client = WaylandClient()
    private val display = WlDisplay(this)

    internal var nextId = 1 // id 1 is reserved for wl_display

    internal fun send(msg: Message) = client.send(msg)
    internal fun receive(): Message? {
        val msg = client.receive()

        if (msg == null) return null

        if (msg.header.objectId == display.objectId) {
            display.processEvent(msg)
            return null
        }

        return msg
    }

    fun connect(path: String = "/run/user/1000/wayland-0") = client.connect(path)
    fun close() = client.close()

    fun getDisplay() = display
}