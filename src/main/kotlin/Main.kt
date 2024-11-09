import wayland.Wayland
import wayland.protocol.wayland.WlCallback
import wayland.protocol.wayland.WlCompositor
import wayland.protocol.wayland.WlShm
import wayland.protocol.xdg.XdgWmBase
import wayland.protocol.xdg.ZXdgDecorationManager
import wayland.protocol.xdg.ZXdgTopLevelDecoration
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

fun main() {
    val wayland = Wayland()
    wayland.connect()

    val display = wayland.getDisplay()
    val registry = display.getRegistry()

    wayland.roundtrip()

    var compositor: WlCompositor? = null
    var shm: WlShm? = null
    var xdgWmBase: XdgWmBase? = null
    var xdgDecorationManager: ZXdgDecorationManager? = null

    registry.getObjects().forEach { (_, obj) ->
        when (obj.interface_) {
            "wl_compositor" -> compositor = registry.bind(obj, WlCompositor::class.java)
            "wl_shm" -> shm = registry.bind(obj, WlShm::class.java)
            "xdg_wm_base" -> xdgWmBase = registry.bind(obj, XdgWmBase::class.java)
            "zxdg_decoration_manager_v1" -> xdgDecorationManager = registry.bind(obj, ZXdgDecorationManager::class.java)
        }
    }

    if (compositor == null || shm == null || xdgWmBase == null || xdgDecorationManager == null) {
        println("[ERROR] Failed to bind required interfaces")
        return
    }

    wayland.roundtrip()

    val gifBytes = File("/home/outofrange/Projects/wayland_kt/src/main/kotlin/meme.gif").readBytes()
    val decoder = GifDecoder()
    val frames = decoder.decode(gifBytes)

    val width = 350
    val height = 350
    val stride = width * 4
    val size = stride * height

    val shmFile = File.createTempFile("wayland_shm", null)
    shmFile.deleteOnExit()

    RandomAccessFile(shmFile, "rw").use { file ->
        file.setLength(size.toLong())
    }

    val fc = FileChannel.open(shmFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)
    val buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, size.toLong())

    val fdField = fc.javaClass.getDeclaredField("fd")
    fdField.isAccessible = true
    val fdObject = fdField.get(fc)

    val fdIntField = fdObject.javaClass.getDeclaredField("fd")
    fdIntField.isAccessible = true
    val trueFd = fdIntField.getInt(fdObject)

    val pool = shm.createPool(trueFd, size)
    val shmBuffer = pool.createBuffer(0, width, height, stride, WlShm.Format.XRGB8888)

    val surface = compositor.createSurface()
    val xdgSurface = xdgWmBase.getXdgSurface(surface)
    val xdgTopLevel = xdgSurface.getToplevel()
    val xdgTopLevelDecoration = xdgDecorationManager.getTopLevelDecoration(xdgTopLevel.objectId)
    xdgTopLevelDecoration.setMode(ZXdgTopLevelDecoration.Mode.SERVER_SIDE)

    xdgTopLevel.setTitle("Simple window with a gif")
    xdgTopLevel.setAppId("org.simple_window")

    var currentFrame = 0
    var callback: WlCallback? = null

    Wayland.running = true

    fun clearColor() {
        buffer.clear()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val offset = (y * stride) + (x * 4)
                buffer.put(offset + 0, 0.toByte())
                buffer.put(offset + 1, 0.toByte())
                buffer.put(offset + 2, 0.toByte())
                buffer.put(offset + 3, 0.toByte())
            }
        }
    }

    while (Wayland.running) {
        wayland.roundtrip()
        val frame = frames[currentFrame]

        if (callback?.done == true) callback = null

        if (callback == null) {
            frame.pixels.rewind()

            clearColor()

            for (y in 0 until frame.height) {
                for (x in 0 until frame.width) {
                    val bufferOffset = (y * stride) + (x * 4)
                    val frameOffset = (y * frame.width + x) * 4

                    val r = frame.pixels.get(frameOffset)
                    val g = frame.pixels.get(frameOffset + 1)
                    val b = frame.pixels.get(frameOffset + 2)
                    val a = frame.pixels.get(frameOffset + 3)

                    // BGRA is required for wl_shm e.g Format.XRGB8888
                    buffer.put(bufferOffset + 0, b)
                    buffer.put(bufferOffset + 1, g)
                    buffer.put(bufferOffset + 2, r)
                    buffer.put(bufferOffset + 3, a)
                }
            }

            callback = surface.frame()
            surface.attach(shmBuffer, 0, 0)
            surface.damage(0, 0, width, height)
            surface.commit()

            currentFrame = (currentFrame + 1) % frames.size
        }

        Thread.sleep(frame.delayMs.toLong())
    }

    fc.close()
    wayland.close()
}