package top.lanscarlos.vulpecula.bacikal.action

import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.LiveData
import top.lanscarlos.vulpecula.bacikal.bacikalSwitch
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture
import java.util.regex.Matcher

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action
 *
 * @author Lanscarlos
 * @since 2023-02-06 11:39
 */
object ActionRegex {

    @BacikalParser(
        id = "regex",
        name = ["regex"]
    )
    fun parser() = bacikalSwitch {
        case("group") {
            combine(
                int(0)
            ) { index ->
                val matcher = this.getVariable<Matcher>("@Matcher") ?: error("No matcher selected.")
                if (index in 0..matcher.groupCount()) {
                    matcher.group(index)
                } else {
                    null
                }
            }
        }

        case("match") {
            combine(
                stringOrList(),
                trim("using", "with", "by", then = text())
            ) { source, pattern ->
                val matcher = pattern.toPattern().matcher(source.format())
                this.variables()["@Matcher"] = matcher
                this.variables()["matcher"] = matcher
                matcher.find().also {
                    this.variables()["found"] = matcher.group()
                    this.variables()["count"] = matcher.groupCount()
                }
            }
        }

        case("matches") {
            combine(
                stringOrList(),
                trim("using", "with", "by", then = text())
            ) { source, pattern ->
                source.format().matches(pattern.toRegex())
            }
        }

        case("find") {
            combine(
                stringOrList(),
                trim("using", "with", "by", then = text()),
                LiveData {
                    if (expectToken("to")) {
                        val action = readAction()
                        Bacikal.Action { frame -> frame.run(action) }
                    } else {
                        expectToken("then")
                        val action = readAction()
                        Bacikal.Action { CompletableFuture.completedFuture(action) }
                    }
                }
            ) { source, pattern, handle ->

                val matcher = pattern.toPattern().matcher(source.format())
                val buffer = StringBuffer()
                val result = mutableListOf<Any?>()

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

                return@combine result
            }
        }

        case("replace", "rep") {
            combine(
                stringOrList(),
                trim("using", "with", "by", then = text()),
                LiveData {
                    if (expectToken("to")) {
                        val action = readAction()
                        Bacikal.Action { frame -> frame.run(action) }
                    } else {
                        expectToken("then")
                        val action = readAction()
                        Bacikal.Action { CompletableFuture.completedFuture(action) }
                    }
                }
            ) { source, pattern, handle ->
                val matcher = pattern.toPattern().matcher(source.format())
                val buffer = StringBuffer()

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
                return@combine if (source is String) {
                    // 原内容为字符串
                    result
                } else {
                    // 原内容为 list
                    result.split('\n').toList()
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

    private val regex = Regex("/(.*)(?<!\\\\)/([imgs])+")

    /**
     * 将字符串解析为正则表达式
     * /${regex}/${options}
     * */
    fun String.decodeToRegex(): Regex {
        val result = regex.matchEntire(this) ?: return this.toRegex()

        // 修饰符
        val modifiers = mutableSetOf<RegexOption>()
        val options = result.groupValues.getOrNull(2) ?: return this.toRegex()

        if ('i' in options) {
            modifiers += RegexOption.IGNORE_CASE
        }
        if ('m' in options) {
            modifiers += RegexOption.MULTILINE
        }
        if ('g' in options || 's' in options) {
            modifiers += RegexOption.DOT_MATCHES_ALL
        }

        return result.groupValues.getOrNull(1)?.toRegex(modifiers) ?: this.toRegex()
    }

}