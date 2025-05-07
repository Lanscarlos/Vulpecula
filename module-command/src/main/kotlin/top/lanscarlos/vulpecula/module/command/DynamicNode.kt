package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentDynamic
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.applicative.applicativeBoolean

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 13:19
 */
class DynamicNode(id: String, parent: Node?, section: Map<*, *>) : Node(id, parent, section) {

    constructor(id: String, parent: Node?, section: ConfigurationSection) : this(id, parent, section.toMap())

    val uncheck: Boolean

    val suggester: Suggester<out Any>?

    val restrictor: Restrictor<out Any>?

    val converter: Converter<out Any>?

    init {
        // 验证配置结构
        require("suggest" !in section || "restrict" !in section) { "It is not allowed to set both suggestion and restriction." }
        uncheck = section["uncheck"].applicativeBoolean(false)
        suggester = section["suggest"]?.let(::parseSuggester)
        restrictor = section["restrict"]?.let(::parseRestrictor)
        converter = suggester ?: restrictor
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
                if (playerRequired) {
                    component.suggestion(bind = ProxyPlayer::class.java, uncheck = uncheck, function = suggester::suggest)
                } else {
                    component.suggestion(bind = ProxyCommandSender::class.java, uncheck = uncheck, function = suggester::suggest)
                }
            }
            restrictor != null -> {
                if (playerRequired) {
                    component.restrict(bind = ProxyPlayer::class.java, function = restrictor::restrict)
                } else {
                    component.restrict(bind = ProxyCommandSender::class.java, function = restrictor::restrict)
                }
            }
        }

        if (executor != null) {
            if (playerRequired) {
                component.execute(bind = ProxyPlayer::class.java, function = executor::execute)
            } else {
                component.execute(bind = ProxyCommandSender::class.java, function = executor::execute)
            }
        }

        // 处理子节点
        for (child in children) {
            component.children += child.build()
        }

        return component
    }

    private fun parseSuggester(suggestion: Any): Suggester<out Any> {
        if (suggestion is List<*>) {
            return ListSuggester(suggestion)
        }
        require(suggestion is String) { "Suggester content is not a string or list." }
        require(suggestion.isNotBlank()) { "Suggester content cannot be blank." }
        if (suggestion[0] != '@' || suggestion.lowercase().startsWith("@script:")) {
            // 启用脚本约束
            return ScriptExecutor(suggestion, chain)
        }
        return when (suggestion.substring(1).lowercase()) {
            "bool", "boolean" -> BooleanSuggester
            "offline" -> OfflinePlayerSuggester
            "player" -> PlayerSuggester
            "world" -> WorldSuggester
            else -> error("Invalid suggester content: $suggestion")
        }
    }

    private fun parseRestrictor(restriction: Any): Restrictor<out Any> {
        require(restriction is String) { "Restrictor content is not a string or list." }
        require(restriction.isNotBlank()) { "Restrictor content cannot be blank." }
        if (restriction[0] != '@' || restriction.lowercase().startsWith("@script:")) {
            // 启用脚本约束
            return ScriptExecutor(restriction, chain)
        }
        return when (restriction.substring(1).lowercase()) {
            "int" -> IntRestrictor
            "double" -> DoubleRestrictor
            else -> error("Invalid restrictor content: $restriction")
        }
    }

}