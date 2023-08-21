package top.lanscarlos.vulpecula.bacikal

import taboolib.module.kether.ScriptActionParser
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-21 10:29
 */

fun <T> bacikal(func: Function<BacikalContext, BacikalFruit<T>>): ScriptActionParser<T> {
    return ScriptActionParser {
        val context = DefaultBacikalContext(this)
        func.apply(context)
    }
}

internal fun <T> bacikalInner(func: BacikalContext.() -> BacikalFruit<T>): ScriptActionParser<T> {
    return ScriptActionParser {
        val context = DefaultBacikalContext(this)
        func(context)
    }
}