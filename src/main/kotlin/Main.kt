import wayland.Wayland
import wayland.protocol.wayland.WlCompositor
import wayland.protocol.wayland.WlShm
import wayland.protocol.xdg.XdgWmBase
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

fun main() {
    val wayland = Wayland()
    wayland.connect()

    val display = wayland.getDisplay()
    val registry = display.getRegistry()

    var compositor: WlCompositor? = null
    var shm: WlShm? = null
    var wmBase: XdgWmBase? = null

    wayland.processEvents()

    registry.getObjects().forEach { (_, obj) ->
        println(obj)
        when (obj.interface_) {
            "wl_compositor" -> compositor = registry.bind(obj, WlCompositor::class.java)
            "wl_shm" -> shm = registry.bind(obj, WlShm::class.java)
            "xdg_wm_base" -> wmBase = registry.bind(obj, XdgWmBase::class.java)
        }
    }

    if (compositor == null || wmBase == null || shm == null) {
        error("Missing required interfaces")
    }

    // IMPORTANT
    val width = 800
    val height = 600
    val stride = width * 4
    val size = stride * height

    val shmFile = File.createTempFile("wayland_shm_buffer", null)
    shmFile.deleteOnExit()

    RandomAccessFile(shmFile, "rw").use { file ->
        file.setLength(size.toLong())
    }

    val fc = FileChannel.open(shmFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)
    val buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0 , size.toLong())

    val fdField = fc.javaClass.getDeclaredField("fd")
    fdField.isAccessible = true
    val fdObject = fdField.get(fc)

    val fdIntField = fdObject.javaClass.getDeclaredField("fd")
    fdIntField.isAccessible = true
    val trueFd = fdIntField.getInt(fdObject)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val offset = (y * stride) + (x * 4)
            buffer.put(offset + 0, 0)          // B
            buffer.put(offset + 1, 0)          // G
            buffer.put(offset + 2, 255.toByte()) // R
            buffer.put(offset + 3, 255.toByte()) // X
        }
    }
    // END IMPORTANT

    val pool = shm.createPool(trueFd, size)
    val shmBuffer = pool.createBuffer(0, width, height, stride, WlShm.Format.XRGB8888)

    val surface = compositor.createSurface()
    val xdgSurface = wmBase.getXdgSurface(surface)
    val topLevel = xdgSurface.getToplevel()

    topLevel.setTitle("Hello from kotlin")
    topLevel.setAppId("com.outofrange.wayland_kt")

    surface.attach(shmBuffer, 0, 0)
    surface.damage(0, 0, width, height)
    surface.commit()

    val frameTime = 16_000_000
    var lastFrame = System.nanoTime()

    while (true) {
        val now = System.nanoTime()
        val delta = now - lastFrame

        if (delta >= frameTime) {
            surface.damage(0, 0, width, height)
            surface.commit()
            lastFrame = now
        }

        wayland.processEvents()

        val remaining = frameTime - (System.nanoTime() - now)
        if (remaining > 0) {
            Thread.sleep(remaining / 1_000_000)
        }
    }

    wayland.close()
}