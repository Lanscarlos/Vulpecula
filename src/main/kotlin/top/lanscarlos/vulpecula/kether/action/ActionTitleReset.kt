package top.lanscarlos.vulpecula.kether.action

import taboolib.module.kether.actionNow
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.playerOrNull
import top.lanscarlos.vulpecula.utils.toBukkit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-12-05 14:05
 */
object ActionTitleReset {

    @VulKetherParser(
        id = "title-reset",
        name = ["title-reset"]
    )
    fun parser() = scriptParser {
        actionNow { playerOrNull()?.toBukkit()?.resetTitle() }
    }

}