package wayland

import wayland.protocol.WaylandObject
import wayland.protocol.wayland.WlDisplay
import kotlin.reflect.KClass

class Wayland {
    companion object {
        var running = false
    }

    private val client = WaylandClient()
    private val display = WlDisplay(this)

    private val objects = mutableMapOf<Int, WaylandObject>()
    internal var nextId = 1  // id 1 is reserved for wl_display

    init {
        objects[1] = display
    }

    internal fun registerObject(obj: WaylandObject): Int {
        objects[obj.objectId] = obj
        return obj.objectId
    }

    internal fun removeObject(id: Int) {
        objects.remove(id)
    }

    fun <T : WaylandObject> findObject(type: KClass<T>): T? {
        return objects.values.firstOrNull { type.isInstance(it) } as? T
    }

    internal fun send(msg: Message, fd: Int? = null) {
        client.send(msg, fd)
    }

    fun dispatch(): Int {
        val msg = client.receive() ?: return 0

        val obj = objects[msg.header.objectId] ?: return -1
        obj.processEvent(msg)

        return 1
    }

    fun roundtrip() {
        val callback = display.sync()
        while (!callback.done) {
            dispatch()
        }
    }

    fun connect(path: String = getDefaultSocketPath()) {
        client.connect(path)
    }

    fun close() {
        client.close()
        objects.clear()
        nextId = 1
    }

    fun getDisplay() = display

    private fun getDefaultSocketPath(): String {
        val xdgRuntimeDir = System.getenv("XDG_RUNTIME_DIR")
            ?: return "/run/user/1000/wayland-0"

        val display = System.getenv("WAYLAND_DISPLAY") ?: "wayland-0"
        return "$xdgRuntimeDir/$display"
    }
}