package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.library.kether.QuestContext
import top.lanscarlos.vulpecula.bacikal.Bacikal
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

    override var artifactFile: File? = null

    override val namespace = mutableListOf<String>()

    override val transfers = linkedMapOf<String, BacikalQuestTransfer>()

    override var compiler = Bacikal.service.questCompiler

    val blocks = linkedMapOf<String, BacikalBlockBuilder>()

    val source = StringBuilder()

    init {
        appendTransfer(FragmentReplacer())
        appendTransfer(CommentEraser())
        appendTransfer(UnicodeEscalator())
    }

    override fun build(): BacikalQuest {
        val main = blocks[QuestContext.BASE_BLOCK] ?: throw IllegalArgumentException("Main block not found.")

        // 注入预设命名空间
        main.namespace += namespace

        // 构建源码
        for (block in blocks.values) {
            source.append(block.build())
            source.append("\n\n")
        }

        // 写入构建文件
        artifactFile?.let {
            try {
                it.writeText(source.toString())
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        return compiler.compile(name, source.toString(), namespace)
    }

    override fun appendBaseBlock(func: BacikalBlockBuilder.() -> Unit) {
        val block = DefaultBlockBuilder(QuestContext.BASE_BLOCK)
        appendBlock(block)
        block.also(func)
    }

    override fun appendBlock(name: String?, content: String) {
        appendBlock(name) {
            appendLiteral(content)
        }
    }

    override fun appendBlock(name: String?, func: BacikalBlockBuilder.() -> Unit) {
        val block = DefaultBlockBuilder(name ?: UUID.randomUUID().toString())
        appendBlock(block)
        block.also(func)
    }

    override fun appendBlock(name: String?, func: Consumer<BacikalBlockBuilder>) {
        val block = DefaultBlockBuilder(name ?: UUID.randomUUID().toString())
        appendBlock(block)
        func.accept(block)
    }

    override fun appendBlock(block: BacikalBlockBuilder) {
        if (blocks.containsKey(block.name)) {
            throw IllegalArgumentException("Block name ${block.name} has already exists.")
        }
        blocks[block.name] = block
    }

    override fun appendTransfer(transfer: BacikalQuestTransfer) {
        transfers[transfer.name] = transfer
    }
}