package wayland.protocol.wayland

import wayland.MessageBuilder
import wayland.MessageHeader
import wayland.Wayland
import wayland.protocol.Event
import wayland.protocol.WaylandObject

class WlDisplay internal constructor(
    private val wl: Wayland,
    override val objectId: Int = 1
) : WaylandObject {
    @Event(opcode = 0)
    fun error(objectId: Int, code: Int, message: String) {
        println("[ERROR] ObjectId: $objectId - ErrorCode: $code - Message: $message")
    }

    @Event(opcode = 1)
    fun deleteId(objectId: Int) {
        wl.removeObject(objectId)
    }

    fun sync(): WlCallback {
        val callbackId = ++wl.nextId

        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 0 // wl_display@sync
            )
        ).putInt(callbackId).build()

        wl.send(msg)

        val callback = WlCallback(wl, callbackId)
        wl.registerObject(callback)

        return callback
    }

    fun getRegistry(): WlRegistry {
        val registryId = ++wl.nextId

        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 1 // wl_display@get_registry
            )
        ).putInt(registryId).build()

        wl.send(msg)

        val registry = WlRegistry(wl, registryId)
        wl.registerObject(registry)

        return registry
    }
}