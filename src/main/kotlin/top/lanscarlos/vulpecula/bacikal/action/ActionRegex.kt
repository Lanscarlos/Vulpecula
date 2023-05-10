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
                val result = this.getVariable<MatchResult>("@MatchResult")
                    ?: this.getVariable<Matcher>("@Matcher")
                    ?: error("No matcher selected.")

                when (result) {
                    is MatchResult -> {
                        result.groupValues.getOrNull(index)
                    }
                    is Matcher -> {
                        if (index in 0..result.groupCount()) {
                            result.group(index)
                        } else {
                            null
                        }
                    }
                    else -> null
                }
            }
        }

        case("match") {
            combine(
                stringOrList(),
                trim("using", "with", "by", then = text())
            ) { source, pattern ->
                val result = pattern.toRegex().find(source.format())
                this.injectVariables(result)
                return@combine result != null
            }
        }

        case("matches") {
            combine(
                stringOrList(),
                trim("using", "with", "by", then = text())
            ) { source, pattern ->
                val result = pattern.toRegex().matchEntire(source.format())
                this.injectVariables(result)
                return@combine result != null
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
                    } else if (expectToken("then")) {
                        val action = readAction()
                        Bacikal.Action { CompletableFuture.completedFuture(action) }
                    } else {
                        Bacikal.Action { CompletableFuture.completedFuture(null) }
                    }
                }
            ) { source, pattern, handle ->

                val found = mutableListOf<Any?>()
                val results = pattern.toRegex().findAll(source.format())

                if (handle is ParsedAction<*>) {
                    for (result in results) {
                        found += this.newFrame(handle)
                            .injectVariables(result)
                            .run<Any?>()
                            .getNow(null)

                        if (this.script().breakLoop) {
                            // 跳出循环
                            this.script().breakLoop = false
                            break
                        }
                    }
                } else if (handle != null) {
                    for (result in results) {
                        found += handle
                    }
                } else {
                    for (result in results) {
                        found += result.value
                    }
                }

                this.injectVariables(results.lastOrNull())
                return@combine found
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
                    } else if (expectToken("then")) {
                        val action = readAction()
                        Bacikal.Action { CompletableFuture.completedFuture(action) }
                    } else {
                        Bacikal.Action { CompletableFuture.completedFuture(null) }
                    }
                }
            ) { source, pattern, handle ->
                val regex = pattern.toRegex()
                val result = if (handle is ParsedAction<*>) {
                    // 使用处理语句替换
                    regex.replace(source.format()) { result ->
                        if (this.script().breakLoop) {
                            // 跳出循环，返回原值
                            result.value
                        } else {
                            this.newFrame(handle)
                                .injectVariables(result)
                                .run<Any?>()
                                .getNow(null)
                                ?.toString() ?: ""
                        }
                    }.also {
                        if (this.script().breakLoop) {
                            this.script().breakLoop = false
                        }
                    }
                } else if (handle != null) {
                    // 使用固定值替换
                    regex.replace(source.format(), handle.toString())
                } else {
                    // 使用空值替换
                    regex.replace(source.format(), "")
                }

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

    private fun ScriptFrame.injectVariables(result: MatchResult?): ScriptFrame {
        this.variables()["@MatchResult"] = result
        this.variables()["Found"] = result?.value
        this.variables()["Group"] = result?.groupValues ?: emptyList<String>()
        this.variables()["Count"] = result?.groupValues?.size

        this.variables()["found"] = result?.value
        this.variables()["count"] = result?.groupValues?.size

        return this
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