package top.lanscarlos.vulpecula.bacikal

import taboolib.module.kether.ScriptActionParser
import top.lanscarlos.vulpecula.bacikal.parser.BacikalContext
import top.lanscarlos.vulpecula.bacikal.parser.DefaultContext
import top.lanscarlos.vulpecula.bacikal.quest.BacikalBlockBuilder
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuest
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuestBuilder
import top.lanscarlos.vulpecula.bacikal.quest.DefaultQuestBuilder
import java.util.function.Consumer
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
fun <T> bacikalParser(func: BacikalContext.() -> BacikalFruit<T>): ScriptActionParser<T> {
    return ScriptActionParser {
        val context = DefaultContext(this)
        func(context)
    }
}

/**
 * 语句处理
 * */
fun <T> bacikalParserAPI(func: Function<BacikalContext, BacikalFruit<T>>): ScriptActionParser<T> {
    return ScriptActionParser {
        val context = DefaultContext(this)
        func.apply(context)
    }
}

fun bacikalQuest(name: String, func: BacikalQuestBuilder.() -> Unit): BacikalQuest {
    return DefaultQuestBuilder(name).also(func).build()
}

fun bacikalQuestAPI(name: String, func: Consumer<BacikalQuestBuilder>): BacikalQuest {
    return DefaultQuestBuilder(name).also { func.accept(it) }.build()
}

fun bacikalQuestSimple(name: String, func: BacikalBlockBuilder.() -> Unit): BacikalQuest {
    return DefaultQuestBuilder(name).also { it.appendBlock(name, func) }.build()
}

fun bacikalQuestSimpleAPI(name: String, func: Consumer<BacikalBlockBuilder>): BacikalQuest {
    return DefaultQuestBuilder(name).also { it.appendBlock(name, func) }.build()
}