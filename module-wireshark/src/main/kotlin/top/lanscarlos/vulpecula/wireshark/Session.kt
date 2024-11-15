package top.lanscarlos.vulpecula.wireshark

import org.bukkit.entity.Player
import taboolib.module.nms.Packet
import taboolib.module.nms.PacketReceiveEvent
import taboolib.module.nms.PacketSendEvent
import java.util.concurrent.ConcurrentHashMap

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.wireshark
 *
 * @author Lanscarlos
 * @since 2024-11-10 16:15
 */
interface Session {

    val id: String

    fun onPacketSend(event: PacketSendEvent)

    fun onPacketReceive(event: PacketReceiveEvent)

    companion object {

        /**
         * 缓存会话
         * */
        internal val registry = ConcurrentHashMap<String, Session>()

//        /**
//         * 获取会话, 若不存在则创建
//         *
//         * @param player 玩家
//         * */
//        fun connect(player: Player): Session {
//            return registry.computeIfAbsent(player.name) { PlayerSession(player) }
//        }
//
//        /**
//         * 获取会话
//         *
//         * @param id 会话 ID
//         * */
//        fun connect(id: String): Session {
//            return registry.computeIfAbsent(id) { NamedSession(id) }
//        }

    }

}