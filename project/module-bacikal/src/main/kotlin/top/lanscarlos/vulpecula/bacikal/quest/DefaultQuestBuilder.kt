package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.warning
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.kether.QuestContext
import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.config.DynamicSection
import java.io.File
import java.util.*
import java.util.function.Consumer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-25 01:13
 */
class DefaultQuestBuilder(override var name: String) : BacikalQuestBuilder {

    override var eraseComments = true

    override var escapeUnicode = true

    override val namespace = Bacikal.service.defaultCompileNamespace.toMutableList()

    override val transfers = mutableListOf<BacikalQuestTransfer>()

    override var artifactFile: File? = null

    val functions = linkedMapOf<String, MutableList<Any>>()

    override fun appendTransfer(transfer: BacikalQuestTransfer) {
        transfers += transfer
    }

    override fun appendContent(content: String) {
        appendContent("main", content)
    }

    override fun appendContent(name: String, content: String) {
        functions.computeIfAbsent(name) { mutableListOf() }.add(content)
    }

    override fun appendContent(section: DynamicSection<*>) {
        appendContent("main", section)
    }

    override fun appendContent(name: String, section: DynamicSection<*>) {
        functions.computeIfAbsent(name) { mutableListOf() }.add(section)
    }

    override fun build(): BacikalQuest {
        // 行号源码映射
        val mapped = mutableListOf<Pair<IntRange, DynamicSection<*>>>()

        if (eraseComments) {
            appendTransfer(CommentEraser)
        }
        if (escapeUnicode) {
            appendTransfer(UnicodeEscalator)
        }

        // 构建源码
        var line = 0
        val source = StringBuilder()
        for ((name, sections) in functions) {

            source.append("def $name = {\n")
            line += 1

            // 构建函数体
            for (section in sections) {
                val content = when (section) {
                    is String -> section
                    is DynamicSection<*> -> {
                        section.getValue()?.toString() ?: "null"
                    }
                    else -> {
                        warning("Unknown section type: ${section.javaClass.name}")
                        section.toString()
                    }
                }
                source.append(content)
                source.append('\n')
                val size = content.split('\n').size
                if (section is DynamicSection<*>) {
                    mapped += (line until line + size) to section
                }
                line += size
            }

            source.append("}\n\n")
            line += 2
        }

        // 转换
        for (transfer in transfers) {
            transfer.transfer(source)
        }

        val artifact = File(getDataFolder(), "output.ks")
        artifact.writeText(source.toString())

        return AnalysisQuestCompiler(mapped).compile(name, source.toString(), namespace)
    }
}