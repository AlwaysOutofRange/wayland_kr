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
    // wayland.connect("/tmp/wayland-proxy-0")
    // wayland.connect("/run/user/1000/wlhax-0")

    val display = wayland.getDisplay()
    val registry = display.getRegistry()

    wayland.roundtrip()

    var compositor: WlCompositor? = null
    var shm: WlShm? = null
    var wmBase: XdgWmBase? = null
    var zxdg: ZXdgDecorationManager? = null

    registry.getObjects().forEach { (_, obj) ->
        when (obj.interface_) {
            "wl_compositor" -> compositor = registry.bind(obj, WlCompositor::class.java)
            "wl_shm" -> shm = registry.bind(obj, WlShm::class.java)
            "xdg_wm_base" -> wmBase = registry.bind(obj, XdgWmBase::class.java)
            "zxdg_decoration_manager_v1" -> zxdg = registry.bind(obj, ZXdgDecorationManager::class.java)
        }
    }

    if (compositor == null || wmBase == null || shm == null || zxdg == null) {
        error("Missing required interfaces")
    }

    wayland.roundtrip()

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
    val topLevelDecoration = zxdg.getTopLevelDecoration(topLevel.objectId)
    topLevelDecoration.setMode(ZXdgTopLevelDecoration.Mode.SERVER_SIDE)

    topLevel.setTitle("Hello from kotlin")
    topLevel.setAppId("com.outofrange.wayland_kt")

    surface.attach(shmBuffer, 0, 0)
    surface.damage(0, 0, width, height)
    surface.commit()

    var lastFrame = System.nanoTime()
    var callback: WlCallback? = null

    Wayland.running = true

    while (Wayland.running) {
        wayland.roundtrip()
        val now = System.nanoTime()

        if (callback?.done == true) callback = null

        if (callback == null) {

            callback = surface.frame()
            surface.attach(shmBuffer, 0, 0)
            surface.damage(0, 0, width, height)
            surface.commit()

            lastFrame = now
        }

        val sleepTime = 16L
        Thread.sleep(sleepTime)
    }

    fc.close()
    wayland.close()
}