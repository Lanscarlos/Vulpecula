package top.lanscarlos.vulpecula.module.bacikal.parser

import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestReader
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.chat.StandardColors

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * 复合语句解析器
 *
 * @author Lanscarlos
 * @since 2024-05-13 17:02
 */
class ComplexActionParser(
    id: String,
    name: String,
    aliases: Array<String>,
    namespace: String,
    description: String,
) : AbstractActionParser(id, name, aliases, namespace, description) {

    companion object {
        const val TAB_BRANCH_NODE = "├── " // 分支节点
        const val TAB_BRANCH_END = "└── " // 末端分支
        const val TAB_BRANCH_CONNECTOR = "│   " // 分支链接
        const val TAB_BRANCH_NONE = "    " // 无分支
    }

    internal val actions: HashMap<String, BacikalActionParser> = hashMapOf()

    internal var defaultAction: BacikalActionParser? = null

    override fun buildStructure(depth: Int): ComponentText {
        val builder = Components.empty()
        for (component in onDrawStructure(depth, 0)) {
            builder.newLine().resetColor().append(component)
        }
        return builder
    }

    override fun onDrawStructure(maxDepth: Int, currentDepth: Int): List<ComponentText> {
        val lines = mutableListOf<ComponentText>()

        // 绘制当前节点
        val name = if (currentDepth == 0) id.replace('.', '-') else name
        lines += Components.text(name).color(StandardColors.RED).resetColor()

        if (actions.isEmpty()) {
            return lines
        }
        if (maxDepth >= 0 && currentDepth >= maxDepth) {
            lines.first().append(" ...")
            return lines
        }
        val indent = " ".repeat(name.length - 3)
        val children = actions.values.toList()
        for ((index, child) in children.withIndex()) {
            val header = if (index != children.lastIndex) TAB_BRANCH_NODE else TAB_BRANCH_END
            val body = if (index != children.lastIndex) TAB_BRANCH_CONNECTOR else TAB_BRANCH_NONE

            val components = (child as AbstractActionParser).onDrawStructure(maxDepth, currentDepth + 1)
            for ((i, component) in components.withIndex()) {
                lines += if (i == 0) {
                    Components.text(indent)
                        .append(header)
                        .append(component)
                        .resetColor()
                } else {
                    Components.text(indent)
                        .append(body)
                        .append(component)
                        .resetColor()
                }
            }
        }
        return lines
    }

    fun addActionParser(parser: BacikalActionParser) {
        val names = listOf(parser.name).plus(parser.aliases)
        for (name in names) {
            actions[name] = parser
        }
    }

    fun addDefaultParser(parser: BacikalActionParser) {
        defaultAction = parser
    }

    override fun <T : Any?> resolve(reader: QuestReader): QuestAction<T> {
        reader.mark()
        val next = reader.nextToken()
        val parser = actions[next] ?: defaultAction ?: error("Unknown action '$next' at $id")
        return parser.resolve(reader)
    }

}