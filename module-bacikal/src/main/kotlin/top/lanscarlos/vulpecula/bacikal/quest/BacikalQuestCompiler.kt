package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.common.platform.function.warning
import taboolib.library.kether.*
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.module.kether.Kether
import taboolib.module.kether.ScriptService
import taboolib.module.kether.action.ActionGet
import taboolib.module.kether.action.ActionLiteral
import taboolib.module.kether.action.ActionProperty
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2024-11-23 12:48
 */
object BacikalQuestCompiler {

    fun compile(source: File, namespace: List<String>): Quest {
        TODO("Not yet implemented.")
    }

    fun compile(source: String, name: String, namespace: List<String>): Quest {
        return InnerLoader().load(
            ScriptService,
            "bacikal_$name",
            source.toByteArray(StandardCharsets.UTF_8),
            listOf("vulpecula", *namespace.toTypedArray()).distinct() // 命名空间去重
        )
    }

    /**
     * @see taboolib.module.kether.KetherScriptLoader
     * */
    class InnerLoader : SimpleQuestLoader() {
        override fun newBlockReader(content: CharArray, service: QuestService<*>, namespace: MutableList<String>): BlockReader {
            return InnerBlockReader(content, service, namespace)
        }
    }

    class InnerBlockReader(content: CharArray, service: QuestService<*>, namespace: MutableList<String>) : BlockReader(content, service, namespace) {
        override fun newActionReader(service: QuestService<*>, namespace: MutableList<String>): SimpleReader {
            return InnerReader(service, this, namespace)
        }
    }

    /**
     * @see taboolib.module.kether.KetherScriptLoader.Reader
     * */
    class InnerReader(service: QuestService<*>, reader: BlockReader, namespace: MutableList<String>) : SimpleReader(service, reader, namespace) {

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
                    wrap(index, ActionLiteral(nextToken()), null, null)
                }
                '{' -> {
                    blockParser.setProperty("index", index)
                    val action = nextAnonAction()
                    index = blockParser.index
                    action as ParsedAction<T>
                }
                '&' -> {
                    val anchor = index
                    skip(1)
                    val token = nextToken()
                    if (token.isNotEmpty() && token[token.length - 1] == ']' && token.indexOf('[') in 1 until token.length) {
                        val i = token.indexOf('[')
                        val element = token.substring(0, i)
                        val propertyKey = token.substring(i + 1, token.length - 1)
                        val innerAction = wrap(anchor, ActionGet<Any>(element), element, null)
                        wrap(anchor, ActionProperty.Get(innerAction, propertyKey), token, null) as ParsedAction<T>
                    } else {
                        wrap(anchor, ActionGet(token), token, null)
                    }
                }
                '*' -> {
                    val anchor = index
                    skip(1)
                    wrap(anchor, ActionLiteral(nextToken()), null, null)
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
                            val parser = optional.get()
                            val innerAction = wrap(index, parser.resolve<Any>(this), element, parser)
                            return wrap(index, ActionProperty.Get(innerAction, propertyKey), token, null) as ParsedAction<T>
                        } else if (Kether.isAllowToleranceParser) {
                            val propertyKey = token.substring(i + 1, token.length - 1)
                            val innerAction = wrap(index, ActionLiteral<Any>(element, true), element, null)
                            return wrap(index, ActionProperty.Get(innerAction, propertyKey), token, null) as ParsedAction<T>
                        }
                        throw LoadError.UNKNOWN_ACTION.create(element)
                    } else {
                        val optional = service.registry.getParser(token, namespace)
                        if (optional.isPresent) {
                            val parser = optional.get()
                            return wrap(index, parser.resolve(this), token, parser)
                        } else if (Kether.isAllowToleranceParser) {
                            return wrap(index, ActionLiteral(token, true), token, null)
                        }
                        throw LoadError.UNKNOWN_ACTION.create(token)
                    }
                }
            }
        }

        fun <T : Any?> wrap(startIndex: Int, action: QuestAction<T>?, token: String?, parser: QuestActionParser?): ParsedAction<T> {
            val length = index - startIndex
            val content = String(content, startIndex, length)
            val properties = mutableMapOf<String, Any>()
            if (token != null) {
                properties["bacikal-content"] = token + content
                properties["bacikal-token"] = token
            } else {
                properties["bacikal-content"] = content
            }
            if (parser != null) {
                properties["bacikal-parser"] = parser.javaClass.name
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
    }

}