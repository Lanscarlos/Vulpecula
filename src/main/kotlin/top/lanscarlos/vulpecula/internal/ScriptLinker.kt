package top.lanscarlos.vulpecula.internal

import taboolib.common.platform.function.console
import taboolib.library.kether.LoadError
import taboolib.module.lang.asLangText
import top.lanscarlos.vulpecula.script.ScriptCompiler

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal.compiler
 *
 * 脚本连接器
 * 用于连接脚本与片段，嵌入片段，并保持关联
 *
 * @author Lanscarlos
 * @since 2022-09-07 10:32
 */
class ScriptLinker(
    val compiler: ScriptCompiler
) {

    private lateinit var content: CharArray
    private var startPos = 0
    private var index = 0

    fun link(buffer: StringBuilder, source: String) {
        content = source.toCharArray()
        reset()

        while (hasNext()) {
            when (peek()) {
                ' ', '/' -> skipBlank()
                '\"', '\'', '&', '*' -> skipToken()
                '\n', '\r', '{', '}', '[', ']', '(', ')' -> skip(1)
                else -> {
                    buffer.append(before())
                    if (!hasNext()) break
                    when (val it = next()) {
                        "fragment" -> {
                            skipBlank()
                            val id = next()
                            val fragment = ScriptFragment.link(compiler, id)

                            if (fragment != null) {
                                buffer.append("\n$fragment\n")
                            } else {
                                buffer.append("\nprint *\"${console().asLangText("Fragment-Not-Found", id)}\"\n")
                            }

                            resetStartPos()
                        }
                        else -> {
                            buffer.append(before())
                        }
                    }
                }
            }
        }

        buffer.append(before())
    }

    private fun resetStartPos() {
        startPos = index
    }

    private fun before(): String {
        return String(content, startPos, index - startPos).also {
            resetStartPos()
        }
    }

    private fun peek(): Char {
        if (index < content.size) {
            return content[index]
        } else {
            throw LoadError.EOF.create()
        }
    }

    private fun hasNext(): Boolean {
        skipBlank()
        return index < content.size
    }

    private fun next(): String {
        if (!hasNext()) {
            throw LoadError.EOF.create()
        }
        skipBlank()
        val begin = index
        while (index < content.size && !Character.isWhitespace(content[index])) {
            index++
        }
        return String(content, begin, index - begin)
    }

    private fun skipToken() {
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

    fun reset() {
        startPos = 0
        index = 0
    }

}