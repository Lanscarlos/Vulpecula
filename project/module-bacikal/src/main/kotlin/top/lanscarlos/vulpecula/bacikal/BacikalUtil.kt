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

/**
 * 语句处理
 * */
fun <T> bacikal(func: BacikalContext.() -> BacikalFruit<T>): ScriptActionParser<T> {
    return ScriptActionParser {
        val context = DefaultBacikalContext(this)
        func(context)
    }
}

/**
 * 语句处理
 * */
fun <T> bacikalAPI(func: Function<BacikalContext, BacikalFruit<T>>): ScriptActionParser<T> {
    return ScriptActionParser {
        val context = DefaultBacikalContext(this)
        func.apply(context)
    }
}