package top.lanscarlos.vulpecula.internal

import taboolib.common.platform.function.console
import taboolib.common.platform.function.info
import taboolib.library.kether.LoadError
import taboolib.module.lang.asLangText

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2022-08-22 16:41
 */
class ScriptBuilder(
    source: String
) {

    private val content = source.toCharArray()
    var startPos = 0
    var index = 0

    fun build(): String {
        val script = StringBuilder()

        while (hasNext()) {
            when (peek()) {
                ' ', '/' -> skipBlank()
                '"', '\'', '&', '*' -> skipToken()
                '\n', '\r', '{', '}', '[', ']', '(', ')' -> skip(1)
                else -> {

                    val before = before()
                    info("before -> \"$before\"")
                    script.append(before)

                    if (!hasNext()) break
                    when (val it = next()) {
                        "fragment" -> {

                            skipBlank()
                            val id = next()
                            val fragment = ScriptFragment.get(id)

                            if (fragment != null) {
                                script.append("\n$fragment\n")
                            } else {
                                script.append("\nprint *\"${console().asLangText("Fragment-Not-Found", id)}\"\n")
                            }

                            resetStartPos()
                        }
                        else -> {
                            val before = before()
                            info("before -> \"$before\"")
                            script.append(before)
                        }
                    }
                }
            }
        }

        val before = before()
        info("before -> \"$before\"")
        script.append(before)

        return script.toString()
    }

    fun resetStartPos() {
        startPos = index
    }

    fun before(): String {
        return String(content, startPos, index - startPos).also {
            resetStartPos()
        }
    }

    fun peek(): Char {
        if (index < content.size) {
            return content[index]
        } else {
            throw LoadError.EOF.create();
        }
    }

    fun hasNext(): Boolean {
        skipBlank();
        return index < content.size;
    }

    fun next(): String {
        if (!hasNext()) {
            throw LoadError.EOF.create()
        }
        skipBlank()
        val begin = index;
        while (index < content.size && !Character.isWhitespace(content[index])) {
            index++
        }
        return String(content, begin, index - begin)
    }

    fun skipToken() {
        when (peek()) {
            '"' -> {
                var cnt = 0
                while (peek() == '"') {
                    cnt++
                    skip(1)
                }
                var met = 0
                for (i in index until content.size) {
                    if (content[i] == '"') {
                        met += 1
                    } else {
                        if (met >= cnt) break
                        else met = 0
                    }
                    skip(1)
                }
                if (met < cnt) throw LoadError.STRING_NOT_CLOSE.create(cnt)
            }
            '\'' -> {
                skip(1)
                while (peek() != '\'') {
                    skip(1)
                }
                skip(1)
            }
            else -> {
                while (index < content.size && !Character.isWhitespace(content[index])) {
                    index++
                }
            }
        }
    }

    private fun skip(step: Int) {
        index += step
    }

    /**
     * 跳过空格以及注释
     *
     * @author TabooLib [taboolib.library.kether.AbstractStringReader]
     * */
    private fun skipBlank() {
        while (index < content.size) {
            if (Character.isWhitespace(content[index])) {
                index++
            } else if (index + 1 < content.size && content[index] == '/' && content[index + 1] == '/') {
                while (index < content.size && content[index] != '\n' && content[index] != '\r') {
                    index++
                }
            } else {
                break
            }
        }
    }
}