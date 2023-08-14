package top.lanscarlos.vulpecula.bacikal.script

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.script
 *
 * @author Lanscarlos
 * @since 2023-05-09 12:45
 */
interface ScriptTransfer {

    /**
     * 转换脚本
     * */
    fun transfer(source: StringBuilder)

    /**
     * 抽取所有字符并清空容器
     * */
    fun StringBuilder.extract(): String {
        val result = toString()
        clear()
        return result
    }
}