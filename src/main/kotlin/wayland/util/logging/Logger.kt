package wayland.util.logging

object Logger {
    enum class Level {
        DEBUG, INFO, WARN, ERROR
    }

    private var level = Level.INFO

    fun setLevel(level: Level) {
        this.level = level
    }

    fun debug(message: String) {
        if (level <= Level.DEBUG) println("[DEBUG] $message")
    }

    fun info(message: String) {
        if (level <= Level.INFO) println("[INFO] $message")
    }

    fun warn(message: String) {
        if (level <= Level.WARN) println("[WARN] $message")
    }

    fun error(message: String) {
        if (level <= Level.ERROR) println("[ERROR] $message")
    }
}