package wayland.protocol.wayland

import wayland.MessageBuilder
import wayland.MessageHeader
import wayland.Wayland
import wayland.protocol.Event
import wayland.protocol.WaylandObject

class WlSurface internal constructor(
    private val wl: Wayland,
    override val objectId: Int
) : WaylandObject {
    private var preferredBufferTransform: Transform? = null

    enum class Transform(val value: Int) {
        NORMAL(0),
        DEG_90(1),
        DEG_180(2),
        DEG_270(3),
        FLIPPED(4),
        FLIPPED_90(5),
        FLIPPED_180(6),
        FLIPPED_270(7);

        companion object {
            fun from(value: Int): Transform = entries.first { it.value == value }
        }
    }

    @Event(opcode = 2)
    fun preferredBufferScale() {
    }

    @Event(opcode = 3)
    fun preferredBufferTransform(transform: Int) {
        this.preferredBufferTransform = Transform.from(transform)
    }

    fun attach(buffer: WlBuffer?, x: Int, y: Int) {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 1 // wl_surface@attach
            )
        ).putInt(buffer?.objectId ?: 0).putInt(x).putInt(y).build()

        wl.send(msg)
    }

    fun damage(x: Int, y: Int, width: Int, height: Int) {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 2 // wl_surface@damage
            )
        ).putInt(x).putInt(y).putInt(width).putInt(height).build()

        wl.send(msg)
    }

    fun commit() {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 6 // wl_surface@commit
            )
        ).build()

        wl.send(msg)
    }

    fun frame(): WlCallback {
        val callbackId = ++wl.nextId

        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 3, // wl_surface@frame
            ),
        ).putInt(callbackId).build()

        wl.send(msg)

        val callback = WlCallback(wl, callbackId)
        wl.registerObject(callback)

        return callback
    }

    fun destroy() {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 0 // wl_surface@destroy
            )
        ).build()

        wl.send(msg)

        wl.removeObject(objectId)
    }
}