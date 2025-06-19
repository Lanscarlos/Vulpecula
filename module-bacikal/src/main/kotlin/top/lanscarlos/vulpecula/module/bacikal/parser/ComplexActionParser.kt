package top.lanscarlos.vulpecula.module.bacikal.parser

import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestReader
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.chat.StandardColors
import top.lanscarlos.vulpecula.module.bacikal.diagram.TreeDiagram

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

    internal val actions: HashMap<String, BacikalActionParser> = hashMapOf()

    internal var defaultAction: BacikalActionParser? = null

    override fun buildStructure(depth: Int): ComponentText {
        val diagram = TreeDiagram<BacikalActionParser>()
        diagram.onDraw {
            Components.text(it.name).color(StandardColors.RED)
        }
        diagram.onIndent {
            it.name.length - 3
        }
        diagram.onTraversal { depth, it ->
            (it as? ComplexActionParser)?.actions?.values?.toList() ?: emptyList()
        }
        return diagram.build(this)
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