package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.component.CommandComponent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.applicative.applicativeBoolean
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 10:31
 */
abstract class Node(val id: String, val parent: Node?, section: Map<*, *>) {

    constructor(id: String, parent: Node?, section: ConfigurationSection) : this(id, parent, section.toMap())

    val name: String = section["name"]?.toString() ?: id

    val permission = section["permission"]?.toString() ?: ""

    val optional = section["optional"].applicativeBoolean(false)

    val playerRequired = section["require-player"].applicativeBoolean(false)

    val children = LinkedList<Node>()

    val senderClass: Class<out ProxyCommandSender> = if (playerRequired) ProxyPlayer::class.java else ProxyCommandSender::class.java

    /**
     * 父子关系链条
     * */
    val chain: List<Node>

    /**
     * 索引
     * */
    val index: Int

    val executor: Executor?

    init {
        val linkedList = LinkedList<Node>()
        var parentNode: Node? = parent
        while (parentNode != null) {
            linkedList.addFirst(parentNode)
            parentNode = parentNode.parent
        }
        chain = linkedList
        index = linkedList.size - 1
        executor = section["execute"]?.let(::parseExecution)
    }

    abstract fun build(): CommandComponent

    private fun parseExecution(value: Any): Executor {
        require(value is String) { "Execution content is not a String." }
        require(value.isNotBlank()) { "Execution content cannot be blank." }
        return ScriptExecutor(value, chain.drop(1).plus(this))
    }

}