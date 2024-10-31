package wayland.util.collections

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AsyncQueue<T> {
    private val channel = Channel<T>(Channel.UNLIMITED)

    suspend fun send(item: T) {
        channel.send(item)
    }

    suspend fun receive(): T {
        return channel.receive()
    }

    fun asFlow(): Flow<T> = flow {
        for (item in channel) {
            emit(item)
        }
    }
}