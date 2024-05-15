package top.lanscarlos.vulpecula.applicative

import org.bukkit.entity.Player

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 14:53
 */
object PlayerListApplicative : ListApplicative<Player>() {

    override fun mapping(instance: Any?): Player {
        if (instance == null) {
            error("Cannot mapping null to Player.")
        }
        return PlayerApplicative.transfer(instance, null) ?: error("Cannot mapping null to Player.")
    }

}