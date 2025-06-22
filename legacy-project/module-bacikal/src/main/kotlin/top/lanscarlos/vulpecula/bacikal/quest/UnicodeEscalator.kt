package top.lanscarlos.vulpecula.bacikal.quest

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-21 21:27
 */
object UnicodeEscalator : BacikalQuestTransfer {

    val REGEX_UNICODE = "\\\\u([A-Za-z0-9]{4})".toRegex()

    override fun transfer(source: StringBuilder) {
        val result = REGEX_UNICODE.replace(source.extract()) {
            Integer.parseInt(it.groupValues[1], 16).toChar().toString()
        }
        source.append(result)
    }

}