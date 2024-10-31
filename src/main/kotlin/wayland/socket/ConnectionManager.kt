package wayland.socket

import wayland.util.collections.IntMap
import wayland.util.result.Result

class ConnectionManager {
    private val connections = IntMap<Connection>()

    suspend fun connectToDisplay(display: String? = null): Result<Connection> {
        return Connection.connect(display).also { result ->
            result.onSuccess { connection ->
                val id = connections.size() + 1
                connections.put(id, connection)
            }
        }
    }

    fun close() {
        connections.forEach { _, connection -> connection.close() }
    }
}
