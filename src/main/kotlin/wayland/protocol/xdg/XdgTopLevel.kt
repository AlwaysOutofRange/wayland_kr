package wayland.protocol.xdg

import wayland.MessageBuilder
import wayland.MessageHeader
import wayland.Wayland
import wayland.protocol.Event
import wayland.protocol.WaylandObject
import wayland.protocol.wayland.WlBuffer
import wayland.protocol.wayland.WlShm
import wayland.protocol.wayland.WlShmPool
import wayland.protocol.wayland.WlSurface

class XdgTopLevel internal constructor(
    private val wl: Wayland,
    override val objectId: Int
) : WaylandObject {
    private val capabilitys = mutableSetOf<WmCapability>()

    enum class WmCapability(val value: Int) {
        WINDOW_MENU(0),
        MAXIMIZE(1),
        FULLSCREEN(2),
        MINIMIZE(3);

        companion object {
            fun fromInt(value: Int): WmCapability? = entries.firstOrNull { it.value == value }
        }
    }

    @Event(opcode = 0)
    fun configure(width: Int, height: Int, states: Int) {
        // Handle window configuration
    }

    @Event(opcode = 1)
    fun close() {
        Wayland.running = false

        val topLevelDecoration = wl.findObject(ZXdgTopLevelDecoration::class)
        val decorationManager = wl.findObject(ZXdgDecorationManager::class)
        val buffer = wl.findObject(WlBuffer::class)
        val shmPool = wl.findObject(WlShmPool::class)
        val shm = wl.findObject(WlShm::class)
        val xdgSurface = wl.findObject(XdgSurface::class)
        val surface = wl.findObject(WlSurface::class)
        val wmBase = wl.findObject(XdgWmBase::class)

        topLevelDecoration?.destroy()
        decorationManager?.destroy()
        buffer?.destroy()
        shmPool?.destroy()
        shm?.release()
        xdgSurface?.destroy()
        surface?.destroy()
        wmBase?.destroy()
    }

    @Event(opcode = 2)
    fun configureBounds(width: Int, height: Int) {
    }

    @Event(opcode = 3)
    fun wmCapabilities(capabilities: Array<Int>) {
        capabilitys.addAll(capabilities.mapNotNull { WmCapability.fromInt(it) })
    }

    fun setTitle(title: String) {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 2, // xdg_toplevel@set_title
            ),
        ).putString(title).build()

        wl.send(msg)
    }

    fun setAppId(appId: String) {
        appId.toByteArray()
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 3, // xdg_toplevel@set_app_id
            ),
        ).putString(appId).build()

        wl.send(msg)
    }

    fun destroy() {
        val msg = MessageBuilder(
            MessageHeader(
                objectId = this.objectId,
                opcode = 0, // xdg_toplevel@destroy
            ),
        ).build()

        wl.send(msg)
    }
}