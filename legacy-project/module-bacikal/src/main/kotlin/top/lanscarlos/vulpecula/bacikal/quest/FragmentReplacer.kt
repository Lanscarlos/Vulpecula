package top.lanscarlos.vulpecula.bacikal.quest

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-21 21:16
 */
class FragmentReplacer(val mapping: Map<String, String>) : BacikalQuestTransfer {

    private val regex = mapping.keys.joinToString(separator = "|", prefix = "\\$\\{(", postfix = ")\\}").toRegex()

    override fun transfer(source: StringBuilder) {
        val result = regex.replace(source.extract()) {
            mapping[it.groupValues[1]] ?: it.value
        }
        source.append(result)
    }
}