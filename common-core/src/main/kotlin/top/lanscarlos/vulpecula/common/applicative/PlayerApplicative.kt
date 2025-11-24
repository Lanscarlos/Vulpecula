package top.lanscarlos.vulpecula.common.applicative

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import top.lanscarlos.vulpecula.common.applicative.exception.ValueConversionException
import top.lanscarlos.vulpecula.common.applicative.exception.TypeConversionException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 14:26
 */
object PlayerApplicative : AbstractApplicative<Player>(Player::class.java) {

    override fun convertOrThrow(instance: Any): Player {
        return when (instance) {
            is Player -> instance
            is OfflinePlayer -> instance.player!!
            is ProxyPlayer -> instance.cast()
            is String -> Bukkit.getPlayerExact(instance) ?: throw ValueConversionException(instance, Player::class.java)
            else -> throw TypeConversionException(instance, Player::class.java)
        }
    }

}