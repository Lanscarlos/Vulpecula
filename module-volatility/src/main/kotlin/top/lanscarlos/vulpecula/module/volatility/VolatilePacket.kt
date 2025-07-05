package top.lanscarlos.vulpecula.module.volatility

import taboolib.module.nms.nmsProxy

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.volatility
 *
 * @author Lanscarlos
 * @since 2025/7/5
 */
interface VolatilePacket {

    fun createPacketPlayOutEntityMetadata(entityId: Int, vararg metadata: Pair<Int, Any>): Any

    companion object : VolatilePacket by nmsProxy("${VolatilePacket::class.java.`package`.name}.Default${VolatilePacket::class.java.simpleName}")

}