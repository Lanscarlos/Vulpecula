package top.lanscarlos.vulpecula.kether.action

import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.*
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture
import java.util.regex.Matcher

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2023-02-06 11:39
 */
object ActionRegex {

    class ActionRegexFind(
        val source: LiveData<List<String>>,
        val pattern: LiveData<String>,
        val handle: ParsedAction<*>?
    ) : ScriptAction<Any?>() {
        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            val future = CompletableFuture<Any?>()

            listOf(
                source.get(frame, emptyList()),
                pattern.getOrNull(frame)
            ).thenTake().thenAccept { args ->
                val source = args[0] as List<*>
                val pattern = args[1]?.toString()?.toPattern() ?: error("No pattern selected.")
                val matcher = pattern.matcher(if (source.size == 1) source[0].toString() else source.joinToString("\n"))

                val result = mutableListOf<Any?>()
                if (handle == null) {
                    while (matcher.find()) {
                        result += matcher.group()
                    }
                } else {
                    // 递归函数
                    fun process() {
                        if (!matcher.find()) return

                        val found = matcher.group()
                        frame.newFrame(handle).also {
                            it.variables()["@Matcher"] = matcher
                            it.variables()["matcher"] = matcher
                            it.variables()["found"] = found
                            it.variables()["count"] = matcher.groupCount()
                        }.run<Any?>().thenAccept {
                            if (frame.script().breakLoop) {
                                // 跳出递归
                                frame.script().breakLoop = false
                            } else {
                                // 存入结果
                                result += it
                                // 继续递归
                                process()
                            }
                        }
                    }

                    process()
                }

                future.complete(result)
            }

            return future
        }
    }

    class ActionRegexReplace(
        val source: ParsedAction<*>,
        val pattern: LiveData<String>,
        val handle: Any
    ) : ScriptAction<Any?>() {
        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            val future = CompletableFuture<Any?>()

            listOf(
                frame.run(source),
                pattern.getOrNull(frame)
            ).thenTake().thenAccept { args ->
                val source = when (val it = args[0]) {
                    is String -> it
                    is Array<*> -> it.mapNotNull { el -> el?.toString() }.joinToString("\n")
                    is Collection<*> -> it.mapNotNull { el -> el?.toString() }.joinToString("\n")
                    else -> ""
                }
                val pattern = args[1]?.toString()?.toPattern() ?: error("No pattern selected.")
                val matcher = pattern.matcher(source)
                val buffer = StringBuffer()

                when (handle) {
                    is LiveData<*> -> {
                        // 简单替换
                        handle.getOrNull(frame).thenAccept {
                            while (matcher.find()) {
                                matcher.appendReplacement(buffer, it?.toString() ?: "")
                            }
                        }
                    }
                    is ParsedAction<*> -> {
                        // 高级替换

                        // 递归函数
                        fun process() {
                            if (!matcher.find()) return

                            val found = matcher.group()
                            frame.newFrame(handle).also {
                                it.variables()["@Matcher"] = matcher
                                it.variables()["matcher"] = matcher
                                it.variables()["found"] = found
                                it.variables()["count"] = matcher.groupCount()
                            }.run<Any?>().thenAccept {
                                if (frame.script().breakLoop) {
                                    // 跳出递归
                                    frame.script().breakLoop = false
                                } else {
                                    // 替换内容
                                    matcher.appendReplacement(buffer, it?.toString() ?: "")
                                    // 继续递归
                                    process()
                                }
                            }
                        }

                        process()
                    }
                }

                val result = matcher.appendTail(buffer).toString()
                if (args[0] is String) {
                    // 原内容为字符串
                    future.complete(result)
                } else {
                    // 原内容为 list
                    future.complete(result.split('\n').toList())
                }
            }

            return future
        }
    }

    @VulKetherParser(
        id = "regex",
        name = ["regex"]
    )
    fun parser() = scriptParser { reader ->
        reader.switch {
            case("find") {
                val source = reader.readStringList()
                reader.hasNextToken("by", "with", "using")
                val pattern = reader.readString()
                val handle = reader.tryNextBlock("then")

                ActionRegexFind(source, pattern, handle)
            }
            case("replace") {
                val source = reader.nextBlock()

                // 简单替换
                val simple = reader.tryReadString("to")

                reader.hasNextToken("by", "with", "using")
                val pattern = reader.readString()

                if (simple == null) {
                    reader.hasNextToken("then")
                    val handle = reader.nextBlock()
                    ActionRegexReplace(source, pattern, handle)
                } else {
                    ActionRegexReplace(source, pattern, simple)
                }

            }
            case("group") {
                val index = reader.readInt()
                actionTake {
                    index.get(this, 0).thenApply {
                        val matcher = this.getVariable<Matcher>("@Matcher") ?: error("No matcher selected.")
                        return@thenApply if (it in 0..matcher.groupCount()) {
                            matcher.group(it)
                        } else {
                            null
                        }
                    }
                }
            }
        }
    }
}