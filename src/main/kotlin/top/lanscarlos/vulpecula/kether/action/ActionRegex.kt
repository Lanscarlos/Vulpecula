package top.lanscarlos.vulpecula.kether.action

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.Parser
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.action.ActionRegex.runHandle
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

    @VulKetherParser(
        id = "regex",
        name = ["regex"]
    )
    fun parser() = buildParserLiteral { reader ->
        case("group") {
            group(
                int(0)
            ) { index ->
                now {
                    val matcher = this.getVariable<Matcher>("@Matcher") ?: error("No matcher selected.")
                    if (index in 0..matcher.groupCount()) {
                        matcher.group(index)
                    } else {
                        null
                    }
                }
            }
        }
        case("match") {
            group(
                stringOrList(),
                expect("by", "with", "using", option = true, then = string())
            ) { source, pattern ->

                val matcher = pattern.toPattern().matcher(source.format())

                now {
                    this.variables()["@Matcher"] = matcher
                    this.variables()["matcher"] = matcher
                    matcher.find().also {
                        this.variables()["found"] = matcher.group()
                        this.variables()["count"] = matcher.groupCount()
                    }
                }
            }
        }
        case("matches") {
            group(
                stringOrList(),
                expect("by", "with", "using", option = true, then = string())
            ) { source, pattern ->
                now {
                    source.format().matches(pattern.toRegex())
                }
            }
        }
        case("find") {
            group(
                stringOrList(),
                expect("by", "with", "using", option = true, then = string()),
                Parser.frame { r ->
                    val action = r.nextBlock()
                    if (reader.hasNextToken("to")) {
                        Parser.Action { frame -> frame.run(action) }
                    } else {
                        reader.hasNextToken("then")
                        Parser.Action.point(action)
                    }
                },
            ) { source, pattern, handle ->
                val matcher = pattern.toPattern().matcher(source.format())
                val buffer = StringBuffer()
                val result = mutableListOf<Any?>()

                now {
                    while (matcher.find()) {
                        if (handle is ParsedAction<*>) {
                            result += this.runHandle(matcher, handle).getNow(null)

                            if (this.script().breakLoop) {
                                // 跳出循环
                                this.script().breakLoop = false
                                break
                            }
                            matcher.appendReplacement(buffer, "")
                            continue
                        } else {
                            result += matcher.group()
                        }
                    }

                    return@now result
                }
            }
        }
        case("replace", "rep") {
            group(
                stringOrList(),
                expect("by", "with", "using", option = true, then = string()),
                Parser.frame { r ->
                    if (reader.hasNextToken("to")) {
                        val action = r.nextBlock()
                        Parser.Action { frame -> frame.run(action) }
                    } else {
                        reader.hasNextToken("then")
                        Parser.Action.point(r.nextBlock())
                    }
                },
            ) { source, pattern, handle ->

                val matcher = pattern.toPattern().matcher(source.format())
                val buffer = StringBuffer()

                now {
                    while (matcher.find()) {
                        if (handle is ParsedAction<*>) {
                            val replacement = this.runHandle(matcher, handle).getNow(null)
                            matcher.appendReplacement(buffer, replacement?.toString() ?: "")

                            if (this.script().breakLoop) {
                                // 跳出循环
                                this.script().breakLoop = false
                                break
                            }
                        } else {
                            matcher.appendReplacement(buffer, handle?.toString() ?: "")
                        }
                    }

                    val result = matcher.appendTail(buffer).toString()
                    return@now if (source is String) {
                        // 原内容为字符串
                        result
                    } else {
                        // 原内容为 list
                        result.split('\n').toList()
                    }
                }
            }
        }
    }

    private fun ScriptFrame.runHandle(matcher: Matcher, handle: ParsedAction<*>): CompletableFuture<Any?> {
        return this.newFrame(handle).also {
            it.variables()["@Matcher"] = matcher
            it.variables()["matcher"] = matcher
            it.variables()["found"] = matcher.group()
            it.variables()["count"] = matcher.groupCount()
        }.run<Any?>()
    }

    private fun Any?.format(): String {
        return when (this) {
            is String -> this
            is Array<*> -> this.mapNotNull { el -> el?.toString() }.joinToString("\n")
            is Collection<*> -> this.mapNotNull { el -> el?.toString() }.joinToString("\n")
            else -> ""
        }
    }

}