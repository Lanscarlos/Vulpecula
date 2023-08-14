package top.lanscarlos.vulpecula.bacikal.script

import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.bacikal.action.ActionRegex.decodeToRegex
import top.lanscarlos.vulpecula.config.DynamicConfig

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.script
 *
 * 注释消除器
 *
 * @author Lanscarlos
 * @since 2023-05-09 12:42
 */
class CommentEraser : ScriptTransfer {

    /**
     * 消除注释
     * */
    override fun transfer(source: StringBuilder) {
        val regex = CommentEraser.regex ?: return
        val result = regex.replace(source.extract(), "")
        source.append(result)
    }

    companion object {

        /**
         * 注释模板
         * */
        val regex by DynamicConfig.bindConfigNode("script-setting.comment-pattern") { value ->
            when (value) {
                is String -> value.decodeToRegex()
                is Array<*> -> value.mapNotNull { it?.toString() }.joinToString(separator = "|").decodeToRegex()
                is Collection<*> -> value.mapNotNull { it?.toString() }.joinToString(separator = "|").decodeToRegex()
                is Map<*, *>,
                is ConfigurationSection -> {
                    val section = if (value is ConfigurationSection) value.toMap() else value as Map<*, *>
                    val single = section["single-line"]?.toString() ?: "(?<!\\\\)//[^\\n]*(?=\\n|\\r)"
                    val multi = section["multi-line"]?.toString() ?: "(?<!\\\\)/\\*[^(\\*/)]*\\*/"
                    "$single|$multi".decodeToRegex()
                }
                else -> null
            }
        }
    }
}