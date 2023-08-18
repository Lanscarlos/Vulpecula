package top.lanscarlos.vulpecula.volatile17

import taboolib.module.nms.nmsProxy

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.volatile17
 *
 * @author Lanscarlos
 * @since 2023-08-18 16:29
 */
interface Volatile17Packet {

    fun createPacketPlayOutEntityMetadata(entityId: Int, vararg metadata: Any): Any

    companion object : Volatile17Packet by nmsProxy("${Volatile17Packet::class.java.`package`.name}.DefaultVolatile17Packet")
}