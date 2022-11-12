package top.lanscarlos.vulpecula.kether.property.entity

import org.bukkit.entity.Player
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property.entity
 *
 * @author Lanscarlos
 * @since 2022-10-18 14:39
 */
@VulKetherProperty(
    id = "player",
    bind = Player::class
)
class PlayerProperty : VulScriptProperty<Player>("player") {

    override fun readProperty(instance: Player, key: String): OpenResult {
        val property: Any? = when (key) {
            "exp" -> instance.exp
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: Player, key: String, value: Any?): OpenResult {
        return OpenResult.failed()
    }
}