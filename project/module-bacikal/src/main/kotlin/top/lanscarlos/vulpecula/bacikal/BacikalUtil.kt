package top.lanscarlos.vulpecula.bacikal

import taboolib.module.kether.ScriptActionParser
import top.lanscarlos.vulpecula.bacikal.parser.BacikalContext
import top.lanscarlos.vulpecula.bacikal.parser.BacikalFruit
import top.lanscarlos.vulpecula.bacikal.parser.DefaultContext
import top.lanscarlos.vulpecula.bacikal.quest.BacikalBlockBuilder
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuest
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuestBuilder
import top.lanscarlos.vulpecula.bacikal.quest.DefaultQuestBuilder

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
 * 构建可执行的任务
 * */
fun bacikalQuest(name: String = System.currentTimeMillis().toString(), func: BacikalQuestBuilder.() -> Unit): BacikalQuest {
    return DefaultQuestBuilder(name).also(func).build()
}

/**
 * 构建仅含主函数的任务
 * */
fun bacikalSimpleQuest(name: String, func: BacikalBlockBuilder.() -> Unit): BacikalQuest {
    return DefaultQuestBuilder(name).also { it.appendBlock(name, func) }.build()
}

/**
 * 构建任务
 * */
fun String.toBacikalQuest(name: String): BacikalQuest {
    return DefaultQuestBuilder(name).also {
        it.appendBlock(name) {
            appendContent(this@toBacikalQuest)
        }
    }.build()
}