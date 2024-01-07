package top.lanscarlos.vulpecula.bacikal.action.internal

import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.BacikalReader
import top.lanscarlos.vulpecula.bacikal.bacikal
import top.lanscarlos.vulpecula.internal.EventDispatcher

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.internal
 *
 * @author Lanscarlos
 * @since 2023-03-25 00:44
 */
object ActionVulpeculaDispatcher {

    @BacikalParser("vulpecula-dispatcher")
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