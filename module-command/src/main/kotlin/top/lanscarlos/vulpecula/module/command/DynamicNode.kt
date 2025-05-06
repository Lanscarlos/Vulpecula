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

    val uncheck = section["uncheck"].applicativeBoolean(false)

    val strategy: Strategy<out Any>? = section["strategy"]?.let(::parseStrategy)

    override fun build(): CommandComponent {
        val component = CommandComponentDynamic(
            comment = name,
            index = index,
            optional = optional,
            permission = permission
        )
        when (strategy) {
            null -> {}
            is Suggester -> {
                if (playerRequired) {
                    component.suggestion(bind = ProxyPlayer::class.java, uncheck = uncheck, function = strategy::suggest)
                } else {
                    component.suggestion(bind = ProxyCommandSender::class.java, uncheck = uncheck, function = strategy::suggest)
                }
            }
            is Restrictor -> {
                if (playerRequired) {
                    component.restrict(bind = ProxyPlayer::class.java, function = strategy::restrict)
                } else {
                    component.restrict(bind = ProxyCommandSender::class.java, function = strategy::restrict)
                }
            }
            else -> error("Invalid strategy: ${strategy.javaClass.name}.")
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

    private fun parseStrategy(value: Any): Strategy<out Any>? {
        require(value is String) { "Strategy content is not a string." }
        require(value.isNotBlank()) { "Strategy content cannot be blank." }

        if (value[0] != '@') {
            // 启用脚本约束
            return ScriptExecutor(value, chain)
        }

        return when (value.substring(1).lowercase()) {
            "*" -> null
            "bool", "boolean" -> BooleanSuggester
            "int" -> IntRestrictor
            "double" -> DoubleRestrictor
            "offline" -> OfflinePlayerSuggester
            "player" -> PlayerSuggester
            "world" -> WorldSuggester
            else -> error("Invalid suggestion content: $value")
        }
    }

}