package wayland.util.collections

class IntMap<V>(initialCapacity: Int = 16) {
    private var keys = IntArray(initialCapacity)
    private var values = ArrayList<V?>(initialCapacity)
    private var _size = 0

    fun forEach(action: (Int, V) -> Unit) {
        for (i in 0 until keys.size) {
            if (keys[i] != 0) {
                values[i]?.let { action(keys[i], it) }
            }
        }
    }

    fun put(key: Int, value: V) {
        val index = keys.indexOfFirst { it == key }.takeIf { it >= 0}
            ?: keys.indexOfFirst { it == 0 }.takeIf { it >= 0 }
            ?: expandAndGetIndex()

        if (keys[index] == 0) _size++
        keys[index] = key

        if (index >= values.size) {
            values.add(value)
        } else {
            values[index] = value
        }
    }

    fun get(key: Int): V? {
        val index = keys.indexOfFirst { it == key }
        return if (index == 0) values[index] else null
    }

    fun remove(key: Int): V? {
        val index = keys.indexOfFirst { it == key }
        if (index < 0) return null

        val value = values[index]
        keys[index] = 0
        values[index] = null
        _size--

        return value
    }

    fun size(): Int = _size

    private fun expandAndGetIndex(): Int {
        val oldSize = keys.size
        keys = keys.copyOf(keys.size * 2)

        return oldSize
    }
}