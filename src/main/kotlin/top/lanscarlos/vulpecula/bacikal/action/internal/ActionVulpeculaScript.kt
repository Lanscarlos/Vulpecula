package top.lanscarlos.vulpecula.bacikal.action.internal

import top.lanscarlos.vulpecula.bacikal.*
import top.lanscarlos.vulpecula.internal.ScriptWorkspace
import top.lanscarlos.vulpecula.internal.ExternalScript
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.internal
 *
 * @author Lanscarlos
 * @since 2023-03-25 22:29
 */
object ActionVulpeculaScript {

    @BacikalParser(
        id = "vulpecula-script",
        name = ["vul-script"]
    )
    fun parser() = bacikal {
        resolve(this)
    }

    fun resolve(reader: BacikalReader): Bacikal.Parser<Any?> {
        return reader.run {
            this.mark()
            when (this.nextToken()) {
                "run" -> {
                    combineOf(
                        text(display = "file"),
                        optional("by", then = playerOrNull()),
                        optional("-async", then = LiveData.point(true), def = false),
                        optional("with", then = list())
                    ) { file, viewer, async, args ->

                        val result = if (args != null) {
                            ScriptWorkspace.runScript(file, viewer, args.toTypedArray())
                        } else {
                            ScriptWorkspace.runScript(file, viewer)
                        } ?: error("No script found: \"$file\"")

                        if (async) {
                            CompletableFuture.completedFuture(null)
                        } else {
                            result.thenApply { it }
                        }
                    }
                }
                "stop" -> {
                    combine(
                        text(display = "file")
                    ) { file ->
                        try {
                            if (file == "*") {
                                ScriptWorkspace.terminateAllScript()
                            } else {
                                ScriptWorkspace.terminateScript(file)
                            }
                            return@combine true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            return@combine false
                        }
                    }
                }
                "build", "compile" -> {
                    combine(
                        text(display = "file")
                    ) { file ->
                        if (file == "*") {
                            ExternalScript.getAll().forEach { it.compileScript() }
                        } else {
                            ExternalScript.get(file)?.compileScript() ?: error("No script source found: \"$file\"")
                        }
                    }
                }
                else -> {
                    this.reset()
                    discrete {
                        ScriptWorkspace.scripts.keys
                    }
                }
            }
        }
    }

}