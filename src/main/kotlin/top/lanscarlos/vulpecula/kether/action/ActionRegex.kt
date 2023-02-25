package top.lanscarlos.vulpecula.kether.action

import taboolib.common.platform.function.info
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.Parser
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.*
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

                val text = when (source) {
                    is String -> source
                    is Array<*> -> source.mapNotNull { el -> el?.toString() }.joinToString("\n")
                    is Collection<*> -> source.mapNotNull { el -> el?.toString() }.joinToString("\n")
                    else -> ""
                }

                val matcher = pattern.toPattern().matcher(text)

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

                val text = when (source) {
                    is String -> source
                    is Array<*> -> source.mapNotNull { el -> el?.toString() }.joinToString("\n")
                    is Collection<*> -> source.mapNotNull { el -> el?.toString() }.joinToString("\n")
                    else -> ""
                }

                now {
                    text.matches(pattern.toRegex())
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
                val text = when (source) {
                    is String -> source
                    is Array<*> -> source.mapNotNull { el -> el?.toString() }.joinToString("\n")
                    is Collection<*> -> source.mapNotNull { el -> el?.toString() }.joinToString("\n")
                    else -> ""
                }

                val matcher = pattern.toPattern().matcher(text)
                val buffer = StringBuffer()

                now {
                    val result = mutableListOf<Any?>()
                    while (matcher.find()) {
                        if (handle is ParsedAction<*>) {
                            val found = matcher.group()
                            result += this.newFrame(handle).also {
                                it.variables()["@Matcher"] = matcher
                                it.variables()["matcher"] = matcher
                                it.variables()["found"] = found
                                it.variables()["count"] = matcher.groupCount()
                            }.run<Any?>().getNow(null)

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

                val text = when (source) {
                    is String -> source
                    is Array<*> -> source.mapNotNull { el -> el?.toString() }.joinToString("\n")
                    is Collection<*> -> source.mapNotNull { el -> el?.toString() }.joinToString("\n")
                    else -> ""
                }

                val matcher = pattern.toPattern().matcher(text)
                val buffer = StringBuffer()

                now {
                    while (matcher.find()) {
                        if (handle is ParsedAction<*>) {
                            val found = matcher.group()
                            val replacement = this.newFrame(handle).also {
                                it.variables()["@Matcher"] = matcher
                                it.variables()["matcher"] = matcher
                                it.variables()["found"] = found
                                it.variables()["count"] = matcher.groupCount()
                            }.run<Any?>().getNow(null)
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

}