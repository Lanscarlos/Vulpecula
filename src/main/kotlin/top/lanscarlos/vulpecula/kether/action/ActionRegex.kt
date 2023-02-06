package top.lanscarlos.vulpecula.kether.action

import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.readInt
import top.lanscarlos.vulpecula.kether.live.readString
import top.lanscarlos.vulpecula.kether.live.readStringList
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
                source.getOrNull(frame),
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
        val source: LiveData<List<String>>,
        val pattern: LiveData<String>,
        val handle: ParsedAction<*>
    ) : ScriptAction<Any?>() {
        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            val future = CompletableFuture<Any?>()

            listOf(
                source.getOrNull(frame),
                pattern.getOrNull(frame)
            ).thenTake().thenAccept { args ->
                val source = args[0] as List<*>
                val pattern = args[1]?.toString()?.toPattern() ?: error("No pattern selected.")
                val matcher = pattern.matcher(if (source.size == 1) source[0].toString() else source.joinToString("\n"))
                val buffer = StringBuffer()

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
                val result = matcher.appendTail(buffer).toString()
                if (source.size > 1) {
                    // 原内容为 list
                    future.complete(result.split('\n').toList())
                } else {
                    // 原内容为字符串
                    future.complete(result)
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
                reader.hasNextToken("by", "with")
                val pattern = reader.readString()
                val handle = reader.tryNextBlock("then")

                ActionRegexFind(source, pattern, handle)
            }
            case("replace") {
                val source = reader.readStringList()
                reader.hasNextToken("by", "with")
                val pattern = reader.readString()
                reader.hasNextToken("then")
                val handle = reader.nextBlock()

                ActionRegexReplace(source, pattern, handle)
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