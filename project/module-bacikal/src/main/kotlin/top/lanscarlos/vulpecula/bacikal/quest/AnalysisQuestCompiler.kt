package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.common.platform.function.info
import taboolib.library.kether.*
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.module.kether.Kether
import taboolib.module.kether.ScriptService
import taboolib.module.kether.action.ActionGet
import taboolib.module.kether.action.ActionLiteral
import taboolib.module.kether.action.ActionProperty
import taboolib.module.kether.printKetherErrorMessage
import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.BacikalError
import top.lanscarlos.vulpecula.config.DynamicSection
import java.nio.charset.StandardCharsets
import java.nio.file.Path

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2024-03-22 13:07
 */
class AnalysisQuestCompiler(val mapped: List<Pair<IntRange, DynamicSection<*>>>) : BacikalQuestCompiler {

    override fun compile(name: String, source: String, namespace: List<String>): BacikalQuest {
        if (Bacikal.analysisSymbolClosure) {
            analysisSymbolClosure(source)
        }
        val quest = InnerLoader().load(
            ScriptService,
            "bacikal_$name",
            source.toByteArray(StandardCharsets.UTF_8),
            listOf("vulpecula", *namespace.toTypedArray())
        )
        return DefaultQuest(quest)
    }

    private fun getPosition(line: Int): String {
        val section = mapped.firstOrNull { line in it.first } ?: return "Unknown position."
        val position = line + section.first.first
        return "${section.second.path}(${section.second.config.path}:$position)"
    }

    private fun getRelativeContent(text: String, line: Int, size: Int = 3): String {
        val lines = text.split("\n")
        val start = (line - size).coerceAtLeast(0)
        val end = line.coerceAtMost(lines.size)
        return lines.subList(start, end).joinToString("\n")
    }

    private fun symbolNotClosed(char: Char, line: Int, expected: Pair<Char, Int>, content: String): LocalizedException {
        info("Symbol not closed: $char, $line, ${expected.first}, ${expected.second}")
        return BacikalError.SYMBOL_NOT_CLOSED.create(expected.first, getPosition(expected.second), getRelativeContent(content, expected.second), char, getPosition(line), getRelativeContent(content, line))
    }

    /**
     * 符号闭合检测
     * @param input 输入文本
     * */
    private fun analysisSymbolClosure(input: String) {
        val stack = mutableListOf<Pair<Char, Int>>()
        var cnt = 0
        var index = 0
        var line = 0
        while (index < input.length) {
            when (val char = input[index]) {
                '\n' -> {
                    // 换行
                    line++
                }
                '\\' -> {
                    // 跳过转义字符
                    index++
                }
                '{', '[' -> {
                    if (stack.lastOrNull()?.first == '\'' || stack.lastOrNull()?.first == '\"') {
                        continue
                    }
                    stack.add(char to line)
                }
                '}' -> {
                    if (stack.lastOrNull()?.first == '\'' || stack.lastOrNull()?.first == '\"') {
                        continue
                    }
                    val cache = stack.removeLastOrNull() ?: throw BacikalError.SYMBOL_NOT_OPENED.create('}', getPosition(line), getRelativeContent(input, line))
                    if (cache.first != '{') {
                        throw symbolNotClosed(char, line, cache, input)
                    }
                }
                ']' -> {
                    if (stack.lastOrNull()?.first == '\'' || stack.lastOrNull()?.first == '\"') {
                        continue
                    }
                    val cache = stack.removeLastOrNull() ?: throw BacikalError.SYMBOL_NOT_OPENED.create(']', getPosition(line), getRelativeContent(input, line))
                    if (cache.first != '[') {
                        throw symbolNotClosed(char, line, cache, input)
                    }
                }
                '\'' -> {
                    if (stack.lastOrNull()?.first == '\"') {
                        continue
                    }
                    // 单引号处理
                    if (stack.lastOrNull()?.first == '\'') {
                        val cache = stack.removeLastOrNull() ?: throw BacikalError.SYMBOL_NOT_OPENED.create('\'', getPosition(line), getRelativeContent(input, line))
                        if (cache.first != '\'') {
                            throw symbolNotClosed(char, line, cache, input)
                        }
                    } else {
                        stack.add(char to line)
                    }
                }
                '\"' -> {
                    if (stack.lastOrNull()?.first == '\'') {
                        continue
                    }
                    var met = 0
                    while (index < input.length && input[index] == '\"') {
                        met++
                        index++
                    }

                    if (cnt > 0) {
                        // 闭合
                        if (met == cnt) {
                            // 恰好闭合
                            val cache = stack.removeLastOrNull() ?: throw BacikalError.SYMBOL_NOT_OPENED.create('\"', getPosition(line), getRelativeContent(input, line))
                            if (cache.first != '\"') {
                                throw symbolNotClosed(char, line, cache, input)
                            }
                        } else if (met > cnt) {
                            // 闭合符号数量更多
                            val cache = stack.removeLastOrNull()
                            if (cache == null) {
                                throw BacikalError.SYMBOL_NOT_OPENED.create('\"', getPosition(line), getRelativeContent(input, line))
                            } else {
                                throw symbolNotClosed(char, line, cache, input)
                            }
                        }
                    } else {
                        // 开合
                        cnt = met
                        stack.add(char to line)
                    }
                }
            }
            index++
        }
    }


    /**
     * @see taboolib.module.kether.KetherScriptLoader
     * */
    inner class InnerLoader : SimpleQuestLoader() {
        override fun newBlockReader(content: CharArray, service: QuestService<*>, namespace: MutableList<String>): BlockReader {
            return InnerBlockReader(content, service, namespace)
        }
    }

    inner class InnerBlockReader(content: CharArray, service: QuestService<*>, namespace: MutableList<String>) : BlockReader(content, service, namespace) {
        override fun newActionReader(service: QuestService<*>, namespace: MutableList<String>): SimpleReader {
            return InnerReader(service, this, namespace)
        }
    }

    /**
     * @see taboolib.module.kether.KetherScriptLoader.Reader
     * */
    inner class InnerReader(service: QuestService<*>, reader: BlockReader, namespace: MutableList<String>) : SimpleReader(service, reader, namespace) {

        /**
         * 行号标记尺
         * */
        private val ruler: List<Int>

        /**
         * 行号
         * */
        val line: Int
            get() = ruler.count { it <= index } + 1

        val position: String
            get() = "${mapped.firstOrNull { line in it.first }?.second?.path}:$line"

        init {
            ruler = mutableListOf()
            for ((i, char) in content.withIndex()) {
                if (char == '\n') {
                    ruler += i
                }
            }
        }

        override fun nextToken(): String {
            return nextTokenBlock().token.replace("\\s", " ")
        }

        override fun nextTokenBlock(): TokenBlock {
            val block = super.nextTokenBlock()
            return TokenBlock(block.token.replace("\\s", " "), block.isBlock)
        }

        override fun <T : Any?> wrap(action: QuestAction<T>?): ParsedAction<T> {
            val wrapped = super.wrap(action)
            wrapped.properties["position"] = position
            return wrapped
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> nextAction(): ParsedAction<T> {
            skipBlank()
            return when (peek()) {
                /*
                 * fix literal
                 * */
                '\'', '\"' -> {
                    wrap(ActionLiteral(nextToken()))
                }
                '{' -> {
                    blockParser.setProperty("index", index)
                    val action = nextAnonAction()
                    index = blockParser.index
                    action as ParsedAction<T>
                }
                '&' -> {
                    skip(1)
                    val token = nextToken()
                    if (token.isNotEmpty() && token[token.length - 1] == ']' && token.indexOf('[') in 1 until token.length) {
                        val i = token.indexOf('[')
                        wrap(ActionProperty.Get(wrap(ActionGet<Any>(token.substring(0, i))), token.substring(i + 1, token.length - 1))) as ParsedAction<T>
                    } else {
                        wrap(ActionGet(token))
                    }
                }
                '*' -> {
                    skip(1)
                    wrap(ActionLiteral(nextToken()))
                }
                else -> {
                    // property player[name]
                    val tokenBlock = nextTokenBlock()
                    val token = tokenBlock.token
                    if (!tokenBlock.isBlock && token.isNotEmpty() && token[token.length - 1] == ']' && token.indexOf('[') in 1 until token.length) {
                        val i = token.indexOf('[')
                        val element = token.substring(0, i)
                        val optional = service.registry.getParser(element, namespace)
                        if (optional.isPresent) {
                            val propertyKey = token.substring(i + 1, token.length - 1)
                            return wrap(ActionProperty.Get(wrap(optional.get().resolve<Any>(this)), propertyKey)) as ParsedAction<T>
                        } else if (Kether.isAllowToleranceParser) {
                            val propertyKey = token.substring(i + 1, token.length - 1)
                            return wrap(ActionProperty.Get(wrap(ActionLiteral<Any>(element, true)), propertyKey)) as ParsedAction<T>
                        }
                        throw BacikalError.UNKNOWN_ACTION.create(element, position)
                    } else {
                        val optional = service.registry.getParser(token, namespace)
                        if (optional.isPresent) {
                            return wrap(optional.get().resolve(this))
                        } else if (Kether.isAllowToleranceParser) {
                            return wrap(ActionLiteral(token, true))
                        }
                        throw BacikalError.UNKNOWN_ACTION.create(token, position)
                    }
                }
            }
        }
    }
}