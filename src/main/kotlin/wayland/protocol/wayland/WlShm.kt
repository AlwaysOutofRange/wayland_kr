package wayland.protocol.wayland

import wayland.MessageBuilder
import wayland.MessageHeader
import wayland.Wayland
import wayland.protocol.WaylandObject

class WlShm internal constructor(
    private val wl: Wayland,
    override val objectId: Int
) : WaylandObject {
    enum class Format(val value: Int) {
        XRGB8888(1),
    }

    fun createPool(fd: Int, size: Int): WlShmPool {
        val shmPoolId = ++wl.nextId

        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 0 // wl_shm@create_pool
            )
        ).putInt(shmPoolId).putInt(size).build()

        wl.send(msg, fd)

        wl.processEvents()

        val pool = WlShmPool(wl, shmPoolId)
        wl.objects.put(shmPoolId, pool)

        return pool
    }
}