package top.lanscarlos.vulpecula.bacikal.script

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.script
 *
 * @author Lanscarlos
 * @since 2023-05-09 12:41
 */
class UnicodeEscalator : ScriptTransfer {

    /**
     * Unicode 转义
     * */
    override fun transfer(source: StringBuilder) {
        val regex = "\\\\u([A-Za-z0-9]{4})".toRegex()
        val result = regex.replace(source.extract()) {
            Integer.parseInt(it.groupValues[1], 16).toChar().toString()
        }
        source.append(result)
    }
}