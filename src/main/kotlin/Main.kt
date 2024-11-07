import wayland.Wayland

fun main() {
    val wayland = Wayland()
    wayland.connect()

    val display = wayland.getDisplay()
    val registry = display.getRegistry()
    registry.processEvents()

    registry.getObjects().forEach { (name, obj) ->
        println("Name: $name - Interface: ${obj.interface_} - Version: ${obj.version}")
    }

    wayland.close()
}