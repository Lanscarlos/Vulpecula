package top.lanscarlos.vulpecula.volatile17

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.volatile17
 *
 * @author Lanscarlos
 * @since 2023-08-18 16:31
 */
class DefaultVolatile17Packet : Volatile17Packet {

    override fun createPacketPlayOutEntityMetadata(entityId: Int, vararg metadata: Any): Any {
        return net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata(
            entityId,
            metadata.map { it as net.minecraft.network.syncher.DataWatcher.b<*> }
        )
    }

}