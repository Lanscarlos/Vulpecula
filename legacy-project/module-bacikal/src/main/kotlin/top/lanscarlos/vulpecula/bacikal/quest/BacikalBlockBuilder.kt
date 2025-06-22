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
     * 脚本内容
     * */
    val content: StringBuilder

    /**
     * 构建源码
     * */
    fun build(): String

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

}