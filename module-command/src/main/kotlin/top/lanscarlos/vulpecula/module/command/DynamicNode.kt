package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentDynamic
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.applicative.applicativeBoolean
import top.lanscarlos.vulpecula.common.core.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 13:19
 */
open class DynamicNode(id: String, parent: Node?, section: Map<*, *>) : Node(id, parent, section) {

    constructor(id: String, parent: Node?, section: ConfigurationSection) : this(id, parent, section.toMap())

    val uncheck: Boolean

    val suggester: Suggester?

    val restrictor: Restrictor?

    init {
        // 验证配置结构
        require("suggest" !in section || "restrict" !in section) {
            // 策略冲突
            asLang("module-command-exception-strategy-conflict", id)
        }
        uncheck = section["uncheck"].applicativeBoolean(false)
        suggester = section["suggest"]?.let(::parseSuggester)
        restrictor = section["restrict"]?.let(::parseRestrictor)
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
                component.suggestion(bind = ProxyCommandSender::class.java, uncheck = uncheck, function = suggester::suggest)
            }
            restrictor != null -> {
                component.restrict(bind = ProxyCommandSender::class.java, function = restrictor::restrict)
            }
        }

        component.execute(bind = ProxyCommandSender::class.java, function = ::execute)

        // 处理子节点
        for (child in children) {
            component.children += child.build()
        }

        return component
    }

    private fun parseSuggester(suggestion: Any): Suggester {
        if (suggestion is List<*>) {
            return ListSuggester(suggestion)
        }
        require(suggestion is String) {
            asLang("module-command-exception-invalid-content", id, "suggest", suggestion.javaClass.name)
        }
        require(suggestion.isNotBlank()) {
            asLang("module-command-exception-invalid-content", id, "suggest", "BLANK#空白")
        }
        if (suggestion[0] != '@' || suggestion.lowercase().startsWith("@script:")) {
            // 启用脚本约束
            return ScriptExecutor(suggestion, ::transformArgs)
        }
        return when (suggestion.substring(1).lowercase()) {
            "bool", "boolean" -> BooleanSuggester
            "offline" -> OfflinePlayerSuggester
            "player" -> PlayerSuggester
            "world" -> WorldSuggester
            else -> error(asLang("module-command-exception-invalid-content", id, "suggest", suggestion))
        }
    }

    private fun parseRestrictor(restriction: Any): Restrictor {
        require(restriction is String) {
            asLang("module-command-exception-invalid-content", id, "restrict", restriction.javaClass.name)
        }
        require(restriction.isNotBlank()) {
            asLang("module-command-exception-invalid-content", id, "restrict", "BLANK#空白")
        }
        if (restriction[0] != '@' || restriction.lowercase().startsWith("@script:")) {
            // 启用脚本约束
            return ScriptExecutor(restriction, ::transformArgs)
        }
        return when (restriction.substring(1).lowercase()) {
            "int" -> IntRestrictor
            "double" -> DoubleRestrictor
            else -> error(asLang("module-command-exception-invalid-content", id, "restrict", restriction))
        }
    }

}