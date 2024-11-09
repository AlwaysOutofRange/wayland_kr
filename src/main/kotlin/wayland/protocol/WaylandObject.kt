package wayland.protocol

import wayland.Message
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

interface WaylandObject {
    val objectId: Int

    fun processEvent(message: Message) {
        val opcode = message.header.opcode.toInt()
        val handler = findEventHandler(opcode)

        if (handler == null) {
            println("No handler for object ${message.header.objectId} and opcode $opcode")
            return
        }

        invokeEventHandler(handler, message)
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
            val typeName = param.type.toString()
            val classifier = param.type.classifier
            when {
                classifier == Int::class -> message.readInt()
                classifier == String::class -> message.readString()
                typeName == "kotlin.Array<kotlin.Int>" -> message.readArray()
                else -> throw IllegalArgumentException("Unsupported parameter type: ${param.type}")
            }
        }.toTypedArray()

        function.call(this, *args)
    }
}