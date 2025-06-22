package top.lanscarlos.vulpecula.legacy.bacikal.action.internal

import top.lanscarlos.vulpecula.legacy.bacikal.Bacikal
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalParser
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalReader
import top.lanscarlos.vulpecula.legacy.bacikal.bacikal
import top.lanscarlos.vulpecula.legacy.internal.EventDispatcher

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.internal
 *
 * @author Lanscarlos
 * @since 2023-03-25 00:44
 */
object ActionVulpeculaDispatcher {

    @BacikalParser(
        id = "vulpecula-dispatcher",
        aliases = ["vul-dispatcher"]
    )
    fun parser() = bacikal {
        resolve(this)
    }

    fun resolve(reader: BacikalReader): Bacikal.Parser<Any?> {
        return reader.run {
            this.mark()
            when (this.nextToken()) {
                "enable", "run" -> {
                    combine(
                        text(display = "dispatcher id")
                    ) { id ->
                        if (id == "*") {
                            EventDispatcher.cache.values.forEach { it.registerListener() }
                        } else {
                            EventDispatcher.get(id)?.registerListener() ?: error("No dispatcher found: \"$id\"")
                        }
                    }
                }
                "disable", "stop" -> {
                    combine(
                        text(display = "dispatcher id")
                    ) { id ->
                        if (id == "*") {
                            EventDispatcher.cache.values.forEach { it.unregisterListener() }
                        } else {
                            EventDispatcher.get(id)?.unregisterListener() ?: error("No dispatcher found: \"$id\"")
                        }
                    }
                }
                else -> {
                    this.reset()
                    discrete {
                        EventDispatcher.cache.keys
                    }
                }
            }
        }
    }
}