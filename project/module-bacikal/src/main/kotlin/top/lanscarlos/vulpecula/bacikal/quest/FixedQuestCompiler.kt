package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.library.kether.*
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.module.kether.Kether
import taboolib.module.kether.ScriptService
import taboolib.module.kether.action.ActionGet
import taboolib.module.kether.action.ActionLiteral
import taboolib.module.kether.action.ActionProperty
import taboolib.module.kether.printKetherErrorMessage
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-20 21:51
 */
object FixedQuestCompiler : BacikalQuestCompiler {

    override fun compile(name: String, source: String, namespace: List<String>): BacikalQuest {
        return try {
            val quest = InnerLoader().load(
                ScriptService,
                "bacikal_$name",
                source.toByteArray(StandardCharsets.UTF_8),
                listOf("vulpecula", *namespace.toTypedArray())
            )
            DefaultQuest(name, quest)
        } catch (ex: Exception) {
            ex.printKetherErrorMessage(true)
            AberrantQuest(name, ex)
        }
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
                        throw LoadError.UNKNOWN_ACTION.create(element)
                    } else {
                        val optional = service.registry.getParser(token, namespace)
                        if (optional.isPresent) {
                            return wrap(optional.get().resolve(this))
                        } else if (Kether.isAllowToleranceParser) {
                            return wrap(ActionLiteral(token, true))
                        }
                        throw LoadError.UNKNOWN_ACTION.create(token)
                    }
                }
            }
        }
    }

}