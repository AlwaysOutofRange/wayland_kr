package wayland.util.collections

class ObjectPool<T>(
    private val factory: () -> T,
    private val reset: (T) -> Unit,
    initialSize: Int = 16
) {
    private val objects = ArrayDeque<T>(initialSize)

    init {
        repeat(initialSize) {
            objects.addLast(factory())
        }
    }

    fun acquire(): T {
        return if (objects.isEmpty()) {
            factory()
        } else {
            objects.removeFirst()
        }
    }

    fun release(obj: T) {
        reset(obj)
        objects.addLast(obj)
    }
}