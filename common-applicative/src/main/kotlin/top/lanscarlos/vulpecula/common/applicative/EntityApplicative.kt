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

    override fun convertOrThrow(instance: Any): Entity {
        return when (instance) {
            is Entity -> instance
            is OfflinePlayer -> instance.player!!
            is ProxyPlayer -> instance.cast()
            is String -> Bukkit.getPlayerExact(instance) ?: throw InvalidValueException(instance, Entity::class.java)
            else -> throw UnsupportedTypeException(instance::class.java, Entity::class.java)
        }
    }

    override fun readProperty(instance: Entity, key: String): Any? {
        errorGetPropertyNotSupported(instance, key)
    }

    override fun writeProperty(instance: Entity, key: String, value: Any?) {
        errorBySetPropertyNotSupported(instance, key)
    }
}