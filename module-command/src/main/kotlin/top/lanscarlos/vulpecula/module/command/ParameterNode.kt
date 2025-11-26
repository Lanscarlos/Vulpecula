package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentDynamic
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.config.boolean
import top.lanscarlos.vulpecula.common.config.convert
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.exception.BlankStringException
import top.lanscarlos.vulpecula.common.exception.InvalidTypeException
import top.lanscarlos.vulpecula.common.exception.UnsupportedValueException
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.module.command.exception.StrategyConflictException
import top.lanscarlos.vulpecula.module.command.restrictor.DoubleRestrictor
import top.lanscarlos.vulpecula.module.command.restrictor.IntRestrictor
import top.lanscarlos.vulpecula.module.command.suggester.BooleanSuggester
import top.lanscarlos.vulpecula.module.command.suggester.ListSuggester
import top.lanscarlos.vulpecula.module.command.suggester.OfflinePlayerSuggester
import top.lanscarlos.vulpecula.module.command.suggester.PlayerSuggester
import top.lanscarlos.vulpecula.module.command.suggester.WorldSuggester
import top.lanscarlos.vulpecula.module.script.Script

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 13:19
 */
class ParameterNode(id: String, parent: Node?, config: ConfigurationSection, script: Script) : Node(id, parent, config) {

    val uncheck: Boolean by config.read("uncheck").boolean(false)

    val suggester: Suggester? by config.read("suggest").convert(::parseSuggester)

    val restrictor: Restrictor? by config.read("restrict").convert(::parseRestrictor)

    override val executor: ScriptExecutor = ScriptExecutor(script, disableSuccessMessage, ::transformArgs)

    init {
        // 验证配置结构
        require(suggester == null || restrictor == null) {
            // 策略冲突
            throw StrategyConflictException(id)
        }
    }

    override fun build(): CommandComponent {
        val component = CommandComponentDynamic(
            comment = name,
            index = index,
            optional = optional,
            permission = permission
        )
        when {
            suggester != null -> {
                component.suggestion(bind = ProxyCommandSender::class.java, uncheck = uncheck, function = suggester!!::suggest)
            }
            restrictor != null -> {
                component.restrict(bind = ProxyCommandSender::class.java, function = restrictor!!::restrict)
            }
        }

        if (playerRequired) {
            component.execute(bind = ProxyPlayer::class.java, function = ::execute)
        } else {
            component.execute(bind = ProxyCommandSender::class.java, function = ::execute)
        }

        // 处理子节点
        for (child in children) {
            component.children += child.build()
        }

        return component
    }

    override fun execute(sender: ProxyPlayer, context: CommandContext<ProxyPlayer>, argument: String) {
        if (!beforeExecute(sender)) {
            return
        }
        super.execute(sender, context, argument)
    }

    override fun execute(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, argument: String) {
        if (!beforeExecute(sender)) {
            return
        }
        super.execute(sender, context, argument)
    }

    private fun beforeExecute(sender: ProxyCommandSender): Boolean {
        if (children.isEmpty() || children.single().optional) {
            return true
        }
        Lang.MODULE_COMMAND_MISSING_ARGUMENT.error(sender, children.single().name)
        return false
    }

    private fun parseSuggester(suggestion: Any?): Suggester? {
        if (suggestion == null) {
            return null
        }
        if (suggestion is List<*>) {
            return ListSuggester(suggestion)
        }
        require(suggestion is String) {
            throw InvalidTypeException(suggestion)
        }
        require(suggestion.isNotBlank()) {
            throw BlankStringException()
        }
        if (suggestion[0] != '@' || suggestion.lowercase().startsWith("@script:")) {
            // 启用脚本约束
            return ScriptExecutor(suggestion, true, ::transformArgs)
        }
        return when (suggestion.substring(1).lowercase()) {
            "bool", "boolean" -> BooleanSuggester
            "offline" -> OfflinePlayerSuggester
            "player" -> PlayerSuggester
            "world" -> WorldSuggester
            else -> throw UnsupportedValueException(suggestion)
        }
    }

    private fun parseRestrictor(restriction: Any?): Restrictor? {
        if (restriction == null) {
            return null
        }
        require(restriction is String) {
            throw InvalidTypeException(restriction)
        }
        require(restriction.isNotBlank()) {
            throw BlankStringException()
        }
        if (restriction[0] != '@' || restriction.lowercase().startsWith("@script:")) {
            // 启用脚本约束
            return ScriptExecutor(restriction, true, ::transformArgs)
        }
        return when (restriction.substring(1).lowercase()) {
            "int" -> IntRestrictor
            "double" -> DoubleRestrictor
            else -> throw UnsupportedValueException(restriction)
        }
    }

}