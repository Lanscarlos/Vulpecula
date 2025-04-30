package top.lanscarlos.vulpecula.module.command

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

    val uncheck = section["uncheck"].applicativeBoolean(false)

    val suggestion: Suggestion? = section["suggestion"]?.let(::parseSuggestion)

    val restriction: Restriction? = section["restriction"]?.let(::parseRestriction)

    override fun build(): CommandComponent {
        val component = CommandComponentDynamic(
            comment = name,
            index = index,
            optional = optional,
            permission = permission
        )
        when {
            suggestion != null && restriction != null -> {
                // 不允许同时设置 suggestion 和 restriction
                error("It is not allowed to set both suggestion and restriction.")
            }
            suggestion != null -> {
                // 设置参数建议
                component.suggestion(
                    bind = senderClass,
                    uncheck = uncheck,
                    function = suggestion::suggest
                )
            }
            restriction != null -> {
                // 设置参数约束
                component.restrict(
                    bind = senderClass,
                    function = restriction::restrict
                )
            }
        }

        if (executor != null) {
            component.execute(bind = senderClass, function = executor::execute)
        }

        // 处理子节点
        for (child in children) {
            component.children += child.build()
        }

        return component
    }

    fun convert(input: String): Any {
        TODO()
    }

    private fun parseRestriction(value: Any): Restriction {
        require(value is String) { "Restriction content is not a string." }
        require(value.isNotBlank()) { "Restriction content cannot be blank." }

        if (value[0] != '@') {
            // 启用脚本约束
            return ScriptExecutor(value, chain)
        }

        return when (value.substring(1).lowercase()) {
            "int" -> IntRestriction
            "double" -> DoubleRestriction
            else -> error("Invalid restriction content: $value")
        }
    }

    private fun parseSuggestion(value: Any): Suggestion {
        require(value is String) { "Suggestion content is not a String." }
        require(value.isNotBlank()) { "Suggestion content cannot be blank." }

        if (value[0] != '@') {
            // 启用脚本建议
            return ScriptExecutor(value, chain)
        }

        return when (value.substring(1).lowercase()) {
            "bool", "boolean" -> BooleanSuggestion
            "offline" -> OfflinePlayerSuggestion
            "player" -> PlayerSuggestion
            "world" -> WorldSuggestion
            else -> error("Invalid suggestion content: $value")
        }
    }

}