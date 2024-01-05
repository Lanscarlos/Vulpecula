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

    override var eraseComment = true

    override var escapeUnicode = true

    override val namespace = Bacikal.service.compileNamespace.toMutableList()

    override val transfers = linkedMapOf<String, BacikalQuestTransfer>()

    override var compiler = Bacikal.service.questCompiler

    override var executor = Bacikal.service.questExecutor

    val blocks = linkedMapOf<String, BacikalBlockBuilder>()

    val source = StringBuilder()

    override fun build(): BacikalQuest {

        if (eraseComment) {
            appendTransfer(CommentEraser())
        }
        if (escapeUnicode) {
            appendTransfer(UnicodeEscalator())
        }

        // 构建源码
        for (block in blocks.values) {
            source.append(block.build())
            source.append("\n\n")
        }

        // 转换
        for (transfer in transfers.values) {
            transfer.transfer(source)
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

    override fun appendMainBlock(func: BacikalBlockBuilder.() -> Unit) {
        val block = DefaultBlockBuilder(QuestContext.BASE_BLOCK)
        appendBlock(block)
        block.also(func)
    }

    override fun appendBlock(name: String?, content: Any) {
        appendBlock(name) {
            appendContent(content)
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
            error("Block name ${block.name} has already exists.")
        }
        blocks[block.name] = block
    }

    override fun appendTransfer(transfer: BacikalQuestTransfer) {
        transfers[transfer.name] = transfer
    }
}