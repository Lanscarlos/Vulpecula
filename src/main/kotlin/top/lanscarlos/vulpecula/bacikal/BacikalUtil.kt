package top.lanscarlos.vulpecula.bacikal

import taboolib.library.kether.LoadError
import taboolib.module.kether.ScriptActionParser

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