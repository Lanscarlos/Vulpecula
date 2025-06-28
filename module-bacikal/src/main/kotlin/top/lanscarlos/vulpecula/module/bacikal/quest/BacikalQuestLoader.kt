package top.lanscarlos.vulpecula.module.bacikal.quest

import taboolib.common.platform.function.warning
import taboolib.library.kether.*
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.module.kether.Kether
import taboolib.module.kether.RemoteActionParser
import taboolib.module.kether.action.ActionGet
import taboolib.module.kether.action.ActionLiteral
import taboolib.module.kether.action.ActionProperty
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalActionParser
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2025-05-03 13:55
 */
class BacikalQuestLoader : SimpleQuestLoader() {

    private lateinit var innerReader: InnerReader
    private lateinit var innerBlockReader: InnerBlockReader
    private val statistic: HashMap<String, HashMap<String, Int>> = hashMapOf()

    fun getStatistic(): HashMap<String, HashMap<String, Int>> {
        return statistic
    }

    fun getParsedMessage(): String {
        return innerReader.parsedContent()
    }

    fun getUnparseMessage(): String {
        return innerReader.unparseContent()
    }

    fun getParsedActions(): List<ParsedAction<*>> {
        return innerBlockReader.actions
    }

    override fun newBlockReader(content: CharArray, service: QuestService<*>, namespace: MutableList<String>): BlockReader {
        return InnerBlockReader(content, service, namespace).also { innerBlockReader = it }
    }

    inner class InnerBlockReader(content: CharArray, service: QuestService<*>, namespace: MutableList<String>) : BlockReader(content, service, namespace) {

        val actions: LinkedList<ParsedAction<*>> = LinkedList()

        override fun newActionReader(service: QuestService<*>, namespace: MutableList<String>): SimpleReader {
            return InnerReader(service, this, namespace).also { innerReader = it }
        }

        override fun readBlock() {
            expect("def")
            val name = nextToken()
            expect("=")
            this.currentBlock = name
            val actions = readActions()
            this.actions += actions
            checkLiteral(actions)
            val block = SimpleQuest.SimpleBlock(name, actions)
            this.processActions(block, actions)
            this.blocks[name] = block
        }

        override fun readActions(): MutableList<ParsedAction<*>> {
            skipBlank()
            val batch: Boolean = peek() == '{'
            if (batch) {
                skip(1)
            }
            val reader: SimpleReader = newActionReader(service, namespace)
            try {
                val list = ArrayList<ParsedAction<*>>()
                while ((batch && reader.hasNext()) || list.isEmpty()) {
                    if (batch && reader.peek() == '}') {
                        reader.invokeMethod<Unit>("skip", 1)
                        this.index = reader.index
                        list.trimToSize()
                        return list
                    }
                    list.add(reader.nextAction<Any?>())
                    reader.mark()
                }
                return list
            } catch (ex: LocalizedException) {
                var source = String(this.content, reader.mark, this.content.size.coerceAtMost(reader.index) - reader.mark).trim()
                // 优化 EOF 错误展示
                if (batch && ex.error == LoadError.EOF) {
                    source = source.substring(0, source.lastIndexOf('}') - 1)
                }
                throw LoadError.BLOCK_ERROR.create(this.currentBlock, lineOf(this.content, reader.mark), source).then(ex)
            } catch (ex: Exception) {
                throw ex
            }
        }

    }

    /**
     * @see taboolib.module.kether.KetherScriptLoader.Reader
     * */
    inner class InnerReader(service: QuestService<*>, reader: BlockReader, namespace: MutableList<String>) : SimpleReader(service, reader, namespace) {

        fun parsedContent(): String {
            return String(content, 0, index)
        }

        fun unparseContent(): String {
            return String(content, index, content.size - index)
        }

        override fun nextToken(): String {
            return super.nextToken().replace("\\s", " ")
        }

        override fun nextTokenBlock(): TokenBlock {
            val block = super.nextTokenBlock()
            return TokenBlock(block.token.replace("\\s", " "), block.isBlock)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> nextAction(): ParsedAction<T> {
            skipBlank()
            return when (peek()) {
                /*
                 * fix literal
                 * */
                '\'', '\"' -> {
                    wrap(index, ActionLiteral(nextToken()), "", null)
                }
                '{' -> {
                    blockParser.setProperty("index", index)
                    val action = nextAnonAction()
                    index = blockParser.index
                    action as ParsedAction<T>
                }
                '&' -> {
                    val startIndex = index
                    skip(1)
                    val token = nextToken()
                    if (token.isNotEmpty() && token[token.length - 1] == ']' && token.indexOf('[') in 1 until token.length) {
                        val i = token.indexOf('[')
                        val element = token.substring(0, i)
                        val propertyKey = token.substring(i + 1, token.length - 1)
                        val innerAction = wrap(startIndex, ActionGet<Any>(element), element, null)
                        wrap(startIndex, ActionProperty.Get(innerAction, propertyKey), token, null) as ParsedAction<T>
                    } else {
                        wrap(startIndex, ActionGet(token), token, null)
                    }
                }
                '*' -> {
                    val startIndex = index
                    skip(1)
                    wrap(startIndex, ActionLiteral(nextToken()), "*", null)
                }
                else -> {
                    val startIndex = index
                    // property player[name]
                    val tokenBlock = nextTokenBlock()
                    val token = tokenBlock.token
                    if (!tokenBlock.isBlock && token.isNotEmpty() && token[token.length - 1] == ']' && token.indexOf('[') in 1 until token.length) {
                        val i = token.indexOf('[')
                        val element = token.substring(0, i)
                        val optional = service.registry.getParser(element, namespace)
                        if (optional.isPresent) {
                            val propertyKey = token.substring(i + 1, token.length - 1)
                            val parser = optional.get()
                            val innerAction = wrap(startIndex, parser.resolve<Any>(this), element, parser)
                            return wrap(startIndex, ActionProperty.Get(innerAction, propertyKey), token, null) as ParsedAction<T>
                        } else if (Kether.isAllowToleranceParser) {
                            val propertyKey = token.substring(i + 1, token.length - 1)
                            val innerAction = wrap(startIndex, ActionLiteral<Any>(element, true), element, null)
                            return wrap(startIndex, ActionProperty.Get(innerAction, propertyKey), token, null) as ParsedAction<T>
                        }
                        throw LoadError.UNKNOWN_ACTION.create(element)
                    } else {
                        val optional = service.registry.getParser(token, namespace)
                        if (optional.isPresent) {
                            val parser = optional.get()
                            return wrap(startIndex, parser.resolve(this), token, parser)
                        } else if (Kether.isAllowToleranceParser) {
                            return wrap(startIndex, ActionLiteral(token, true), token, null)
                        }
                        throw LoadError.UNKNOWN_ACTION.create(token)
                    }
                }
            }
        }

        fun <T : Any?> wrap(startIndex: Int, action: QuestAction<T>?, header: String, parser: QuestActionParser?): ParsedAction<T> {
            val length = index - startIndex
            val content = String(content, startIndex, length).replace("[\\s|\\n]+".toRegex(), " ")
            val properties = mutableMapOf<String, Any>()
            properties["BACIKAL_HEADER"] = header
            properties["BACIKAL_CONTENT"] = content
            properties["BACIKAL_START_INDEX"] = startIndex
            properties["BACIKAL_END_INDEX"] = index
            properties["BACIKAL_START_LINE"] = lineOf(this.content, startIndex)
            properties["BACIKAL_END_LINE"] = lineOf(this.content, index)
            if (parser != null) {
                properties["BACIKAL_PARSER"] = parser.javaClass.name
            }
            when (parser) {
                null -> {}
                is BacikalActionParser -> {
                    val innerMap = statistic.computeIfAbsent("Bacikal") { hashMapOf() }
                    innerMap.compute(parser.id) { _, value ->
                        value?.plus(1) ?: 1
                    }
                }
                is RemoteActionParser -> {
                    val innerMap = statistic.computeIfAbsent("Remote") { hashMapOf() }
                    innerMap.compute(parser.action) { _, value ->
                        value?.plus(1) ?: 1
                    }
                }
                else -> {
                    val innerMap = statistic.computeIfAbsent("Local") { hashMapOf() }
                    innerMap.compute(header) { _, value ->
                        value?.plus(1) ?: 1
                    }
                }
            }
            return wrap(action, properties)
        }

        fun <T : Any?> wrap(action: QuestAction<T>?, properties: Map<String, Any>): ParsedAction<T> {
            return ParsedAction(action, properties)
        }

        override fun <T : Any?> wrap(action: QuestAction<T>?): ParsedAction<T> {
            warning("Calling wrap(action: QuestAction<T>?) in InnerReader is not allowed.")
            val properties = mutableMapOf<String, Any>()
            return ParsedAction(action, properties)
        }

        private fun lineOf(chars: CharArray, index: Int): Int {
            var line = 0
            for (i in 0 until index) {
                if (chars[i] == '\n') {
                    ++line
                }
            }
            return line
        }
    }

}