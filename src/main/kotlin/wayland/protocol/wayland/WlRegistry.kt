package wayland.protocol.wayland

import wayland.Wayland
import wayland.protocol.Event
import wayland.protocol.WaylandObject

class WlRegistry internal constructor(
    private val wayland: Wayland,
    override val objectId: Int
) : WaylandObject {
    private val registryObjects = mutableMapOf<Int, RegistryObject>()

    data class RegistryObject(
        val name: Int,
        val interface_: String,
        val version: Int
    )

    // This seems unused but its actually used look at the WaylandObject interface and the Event annotation
    @Event(opcode = 0)
    fun global(name: Int, interface_: String, version: Int) {
        registryObjects[name] = RegistryObject(name, interface_, version)
    }

    fun processEvents() {
        while (true) {
            val msg = wayland.receive()

            if (msg == null) break
            if (msg.header.objectId != this.objectId) continue

            processEvent(msg)
        }
    }

    fun getObjects(): Map<Int, RegistryObject> = registryObjects.toMap()
}