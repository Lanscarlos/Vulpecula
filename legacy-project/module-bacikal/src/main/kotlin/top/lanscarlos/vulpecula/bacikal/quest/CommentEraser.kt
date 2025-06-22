package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.config.bindConfigSection

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-21 21:16
 */
object CommentEraser : BacikalQuestTransfer {

    val REGEX_COMMENT by bindConfigSection("bacikal.comment-pattern") { value ->
        when (value) {
            is String -> value.toRegex()
            is Array<*> -> value.mapNotNull { it?.toString() }.joinToString(separator = "|").toRegex()
            is Collection<*> -> value.mapNotNull { it?.toString() }.joinToString(separator = "|").toRegex()
            is Map<*, *>,
            is ConfigurationSection -> {
                val section = if (value is ConfigurationSection) value.toMap() else value as Map<*, *>
                val single = section["single-line"]?.toString() ?: "(?<!\\\\)//[^\\n]*(?=\\n|\\r)"
                val multi = section["multi-line"]?.toString() ?: "(?<!\\\\)/\\*[^(\\*/)]*\\*/"
                "$single|$multi".toRegex()
            }
            else -> null
        }
    }

    override fun transfer(source: StringBuilder) {
        val regex = REGEX_COMMENT ?: return
        val result = regex.replace(source.extract(), "")
        source.append(result)
    }

}