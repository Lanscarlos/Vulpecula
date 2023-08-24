package top.lanscarlos.vulpecula.bacikal.quest

import java.io.File
import java.util.function.Consumer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-25 01:13
 */
interface BacikalQuestBuilder {

    /**
     * 任务名称
     * */
    var name: String

    /**
     * 构建文件
     * */
    var artifactFile: File?

    val namespace: MutableList<String>

    val transfers: MutableMap<String, BacikalQuestTransfer>

    var compiler: BacikalQuestCompiler

    fun build(): BacikalQuest

    fun appendBlock(block: BacikalBlockBuilder)

    fun appendBlock(name: String? = null, func: BacikalBlockBuilder.() -> Unit)

    fun appendBlock(name: String? = null, func: Consumer<BacikalBlockBuilder>)

    fun appendTransfer(transfer: BacikalQuestTransfer)
}