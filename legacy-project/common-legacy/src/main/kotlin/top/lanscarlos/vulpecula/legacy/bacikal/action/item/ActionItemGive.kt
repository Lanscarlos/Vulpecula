package top.lanscarlos.vulpecula.legacy.bacikal.action.item

import taboolib.platform.util.giveItem
import top.lanscarlos.vulpecula.legacy.utils.playerOrNull
import top.lanscarlos.vulpecula.legacy.utils.toBukkit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.item
 *
 * @author Lanscarlos
 * @since 2023-03-27 23:11
 */
object ActionItemGive : ActionItem.Resolver {

    override val name: Array<String> = arrayOf("give")

    override fun resolve(reader: ActionItem.Reader): ActionItem.Handler<out Any?> {
        return reader.transfer {
            combine(
                source(),
                optional("to", then = player()),
                optional("with", "repeat", then = int(), def = 1)
            ) { item, player, repeat ->
                val target = player ?: this.playerOrNull()?.toBukkit() ?: error("No player selected.")
                target.giveItem(item, repeat)
                item
            }
        }
    }
}