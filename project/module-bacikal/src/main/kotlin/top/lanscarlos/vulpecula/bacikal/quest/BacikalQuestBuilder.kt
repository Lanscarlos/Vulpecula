package top.lanscarlos.vulpecula.bacikal.quest

import top.lanscarlos.vulpecula.config.DynamicSection
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
     * 是否擦除注释
     * */
    var eraseComments: Boolean

    /**
     * 是否转义 Unicode
     * */
    var escapeUnicode: Boolean

    /**
     * 编译时命名空间
     * */
    val namespace: MutableList<String>

    /**
     * 转换器
     * */
    val transfers: MutableList<BacikalQuestTransfer>

    var artifactFile: File?

    /**
     * 向主函数追加脚本内容
     *
     * @param content 脚本内容
     * */
    fun appendContent(content: String)

    /**
     * 向指定函数追加脚本内容
     *
     * @param name 函数名
     * @param content 脚本内容
     * */
    fun appendContent(name: String, content: String)

    /**
     * 向主函数追加脚本内容
     *
     * @param section 脚本内容
     * */
    fun appendContent(section: DynamicSection<*>)

    /**
     * 向指定函数追加脚本内容
     *
     * @param name 函数名
     * @param section 脚本内容
     * */
    fun appendContent(name: String, section: DynamicSection<*>)

    /**
     * 添加转换器
     * */
    fun appendTransfer(transfer: BacikalQuestTransfer)

    /**
     * 构建任务
     * */
    fun build(): BacikalQuest
    /**
     * 添加函数块
     * */

//    fun appendBlock(block: BacikalBlockBuilder)
    /**
     * 添加主函数块
     * */

//    fun appendMainBlock(func: BacikalBlockBuilder.() -> Unit)
    /**
     * 添加函数块
     * */

//    fun appendBlock(name: String? = null, content: Any)
    /**
     * 添加函数块
     * */

//    fun appendBlock(name: String? = null, func: BacikalBlockBuilder.() -> Unit)
    /**
     * 添加函数块
     * */

//    fun appendBlock(name: String? = null, func: Consumer<BacikalBlockBuilder>)
}