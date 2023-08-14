package top.lanscarlos.vulpecula.bacikal.action.target.selector

import taboolib.module.kether.script
import taboolib.platform.type.BukkitPlayer
import top.lanscarlos.vulpecula.bacikal.action.target.ActionTarget

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.target.selector
 *
 * @author Lanscarlos
 * @since 2023-03-22 21:41
 */
object ActionTargetSelectSelf : ActionTarget.Resolver {

    override val name: Array<String> = arrayOf("Self")

    override fun resolve(reader: ActionTarget.Reader): ActionTarget.Handler<out Any?> {
        return reader.transfer {
            combine(
                source()
            ) { target ->

                // 加入自身
                when (val it = this.script().sender) {
                    is BukkitPlayer -> target += it.player
                    else -> {
                        if (it != null) target += it
                    }
                }

                target
            }
        }
    }
}