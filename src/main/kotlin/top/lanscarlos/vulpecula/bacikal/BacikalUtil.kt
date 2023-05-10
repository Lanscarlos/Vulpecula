package top.lanscarlos.vulpecula.bacikal

import taboolib.library.kether.LoadError
import taboolib.module.kether.ScriptActionParser
import top.lanscarlos.vulpecula.bacikal.script.BacikalScript
import top.lanscarlos.vulpecula.bacikal.script.BacikalScriptBuilder
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-02-26 16:41
 */

fun <T> bacikal(func: BacikalReader.() -> Bacikal.Parser<T>): ScriptActionParser<T> {
    return ScriptActionParser { func(BacikalReader((this))).resolve() }
}

fun bacikalSwitch(func: BacikalReader.() -> Unit): ScriptActionParser<Any?> {
    return ScriptActionParser {
        val dsl = BacikalReader(this).also(func)
        this.mark()
        val next = this.nextToken()
        val method = dsl.methods[next] ?: this.reset().let { dsl.other }
        ?: throw LoadError.NOT_MATCH.create("[${dsl.methods.keys.joinToString(", ")}]", next)

        method().resolve()
    }
}

fun buildBacikalScript(namespace: List<String> = emptyList(), compile: Boolean = true, func: BacikalScriptBuilder.() -> Unit): BacikalScript {
    return BacikalScript(BacikalScriptBuilder().also(func).build(), namespace, compile)
}

/**
 * 联合
 * */
fun List<CompletableFuture<*>>.union(): CompletableFuture<List<Any?>> {
    if (this.isEmpty()) {
        // 队列为空
        return CompletableFuture.completedFuture(emptyList())
    } else if (this.size == 1) {
        // 队列仅有一个
        return this[0].thenApply { listOf(it) }
    }

    var previous = this[0]
    for (index in 1 until this.size) {
        val current = this[index]
        previous = if (previous.isDone) {
            current
        } else {
            previous.thenCompose { current }
        }
    }

    return if (previous.isDone) {
        CompletableFuture.completedFuture(
            this.map { it.getNow(null) }
        )
    } else {
        previous.thenApply {
            this.map { it.getNow(null) }
        }
    }
}