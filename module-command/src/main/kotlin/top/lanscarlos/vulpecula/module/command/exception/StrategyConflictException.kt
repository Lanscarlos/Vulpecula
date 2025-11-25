package top.lanscarlos.vulpecula.module.command.exception

import top.lanscarlos.vulpecula.common.exception.AbstractLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command.exception
 *
 * @author Lanscarlos
 * @since 2025/11/25
 */
class StrategyConflictException(nodeId: String) : AbstractLocalizedException() {

    override val lang: Lang = Lang.MODULE_COMMAND_STRATEGY_CONFLICT

    override val arguments: Array<Any> = arrayOf(nodeId)

}