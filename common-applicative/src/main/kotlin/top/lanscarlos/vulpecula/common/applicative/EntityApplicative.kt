package top.lanscarlos.vulpecula.common.applicative

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Entity
import taboolib.common.platform.ProxyPlayer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 15:10
 */
object EntityApplicative : AbstractApplicative<Entity>(Entity::class.java) {

    override fun convertOrNull(instance: Any?): Entity? {
        return when (instance) {
            is Entity -> instance
            is OfflinePlayer -> instance.player
            is ProxyPlayer -> instance.castSafely()
            is String -> Bukkit.getPlayerExact(instance)
            else -> null
        }
    }

    override fun readProperty(instance: Entity, key: String): Any? {
        failedByGetPropertyNotSupported(instance, key)
    }

    override fun writeProperty(instance: Entity, key: String, value: Any?) {
        failedBySetPropertyNotSupported(instance, key)
    }
}