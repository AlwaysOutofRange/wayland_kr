package wayland.protocol.wayland

import wayland.MessageBuilder
import wayland.MessageHeader
import wayland.Wayland
import wayland.protocol.Event
import wayland.protocol.WaylandObject

class WlRegistry internal constructor(
    private val wl: Wayland,
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
        registryObjects.put(name, RegistryObject(name, interface_, version))
    }

    fun <T : WaylandObject> bind(obj: RegistryObject, interfaceClass: Class<T>): T {
        val newClientId = ++wl.nextId

        val msg = MessageBuilder(
           MessageHeader(
               objectId = this.objectId,
               opcode = 0 // wl_registry@bind
           )
        ).putInt(obj.name).putString(obj.interface_).putInt(obj.version).putInt(newClientId).build()

        wl.send(msg)

        val constructor = interfaceClass.getDeclaredConstructor(Wayland::class.java, Int::class.java)
        constructor.isAccessible = true
        val obj = constructor.newInstance(wl, newClientId)

        wl.objects.put(newClientId, obj)

        return obj
    }


    fun getObjects(): Map<Int, RegistryObject> = registryObjects
}