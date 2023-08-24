package top.lanscarlos.vulpecula.bacikal.quest

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-25 01:04
 */
interface BacikalBlockBuilder {

    /**
     * 主函数名
     * */
    var name: String

    /**
     * 预设命名空间
     * */
    val namespace: MutableList<String>

    /**
     * 前置处理
     * */
    val preprocessor: StringBuilder

    /**
     * 后置处理
     * */
    val postprocessor: StringBuilder

    /**
     * 预设变量
     * */
    val variables: MutableMap<String, StringBuilder>

    /**
     * 脚本内容
     * */
    val content: StringBuilder

    /**
     * 条件体
     * */
    val condition: StringBuilder

    /**
     * 条件假值分支
     * */
    val deny: StringBuilder

    /**
     * 异常捕捉
     * */
    val exceptions: MutableMap<String, StringBuilder> // catch -> handle

    /**
     * 构建源码
     * */
    fun build(): String

    /**
     * 追加变量
     * */
    fun appendVariables(value: Any?)

    /**
     * 追加前置处理
     * */
    fun appendPreprocessor(value: Any?)

    /**
     * 追加后置处理
     * */
    fun appendPostprocessor(value: Any?)

    /**
     * 追加内容
     * */
    fun appendContent(value: Any?)

    /**
     * 追加文本
     * */
    fun appendLiteral(value: String)

    /**
     * 追加条件
     * */
    fun appendCondition(value: Any?)

    /**
     * 追加条件假值分支
     * */
    fun appendDeny(value: Any?)

    /**
     * 追加异常捕捉
     * */
    fun appendExceptions(value: Any?)

    /**
     * 追加内容
     * */
    fun StringBuilder.appendSection(section: Any)
}