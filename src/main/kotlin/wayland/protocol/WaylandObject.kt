package wayland.protocol

import wayland.Message
import kotlin.reflect.KFunction
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.findAnnotation

interface WaylandObject {
    val objectId: Int

    fun processEvent(message: Message) {
        val opcode = message.header.opcode.toInt()
        val handler = findEventHandler(opcode)

        handler?.let { invokeEventHandler(it, message) }
    }

    private fun findEventHandler(opcode: Int): KFunction<*>? {
        return this::class.members
            .filterIsInstance<KFunction<*>>()
            .find { func ->
                func.hasAnnotation<Event>() &&
                func.findAnnotation<Event>()?.opcode == opcode
            }
    }

    private fun invokeEventHandler(function: KFunction<*>, message: Message) {
        val parameters = function.parameters.drop(1) // Skip the first parameter (this)
        val args = parameters.map { param ->
            when (param.type.classifier) {
                Int::class -> message.readInt()
                String::class -> message.readString()
                else -> throw IllegalArgumentException("Unsupported parameter type: ${param.type}")
            }
        }.toTypedArray()

        function.call(this, *args)
    }
}