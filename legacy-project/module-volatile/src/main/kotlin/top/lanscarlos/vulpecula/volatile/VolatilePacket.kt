package top.lanscarlos.vulpecula.volatile

import taboolib.module.nms.nmsProxy

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.volatile
 *
 * @author Lanscarlos
 * @since 2023-08-10 11:04
 */
interface VolatilePacket {

    fun createPacketPlayOutEntityMetadata(entityId: Int, vararg metadata: Pair<Int, Any>): Any

    companion object : VolatilePacket by nmsProxy("${VolatilePacket::class.java.`package`.name}.DefaultVolatilePacket")

}