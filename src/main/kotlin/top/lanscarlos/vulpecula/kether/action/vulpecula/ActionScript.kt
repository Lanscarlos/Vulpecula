package top.lanscarlos.vulpecula.kether.action.vulpecula

import org.bukkit.entity.Entity
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.readString
import top.lanscarlos.vulpecula.kether.live.tryReadEntity
import top.lanscarlos.vulpecula.script.VulScript
import top.lanscarlos.vulpecula.script.ScriptWorkspace
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.thenTake
import top.lanscarlos.vulpecula.utils.tryNextActionList
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-12-24 13:08
 */
object ActionScript {

    class ActionScriptRun(
        val file: LiveData<String>,
        val viewer: LiveData<Entity>?,
        val async: Boolean,
        val args: List<ParsedAction<*>>?
    ) : ScriptAction<Any?>() {
        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            val future = CompletableFuture<Any?>()

            listOf(
                file.getOrNull(frame),
                viewer?.getOrNull(frame),
                *(args ?: emptyList()).map { frame.run(it) }.toTypedArray()
            ).thenTake().thenAccept { args ->
                val id = args[0]?.toString() ?: error("No script file found: \"${args[0]}\"")
                val result = if (args.size > 2) {
                    // 有参数
                    ScriptWorkspace.runScript(id, args[1], *args.subList(2, args.size).toTypedArray())
                } else {
                    // 无参数
                    ScriptWorkspace.runScript(id, args[1])
                }

                if (async) {
                    future.complete(null)
                } else {
                    result?.thenAccept { future.complete(it) } ?: future.complete(null)
                }
            }

            return future
        }
    }

    /**
     * vul script run $file by $viewer -async with [ args... ]
     *
     * vul script stop *
     * vul script stop $file
     *
     * vul script compile/build *
     * vul script compile/build $file
     * */
    fun parse(reader: QuestReader): ScriptAction<*> {
        return reader.switch {
            case("run") {
                val file = reader.readString()
                val viewer = reader.tryReadEntity("by")
                val async = reader.hasNextToken("-async")
                val args = reader.tryNextActionList("with")

                ActionScriptRun(file, viewer, async, args)
            }

            case("stop") {
                if (reader.hasNextToken("*")) {
                    actionNow {
                        try {
                            ScriptWorkspace.terminateAllScript()
                            return@actionNow true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            return@actionNow false
                        }
                    }
                } else {
                    val file = reader.readString()
                    actionTake {
                        file.getOrNull(this).thenApply {
                            try {
                                ScriptWorkspace.terminateScript(it ?: return@thenApply false)
                                return@thenApply true
                            } catch (e: Exception) {
                                e.printStackTrace()
                                return@thenApply false
                            }
                        }
                    }
                }
            }

            case("compile", "build") {
                if (reader.hasNextToken("*")) {
                    actionNow {
                        VulScript.getAll().forEach { it.compileScript() }
                        return@actionNow true
                    }
                } else {
                    val file = reader.readString()
                    actionTake {
                        file.getOrNull(this).thenApply {
                            val id = it ?: return@thenApply false
                            VulScript.get(id)?.compileScript() ?: return@thenApply false
                            return@thenApply true
                        }
                    }
                }
            }
        }
    }

    @VulKetherParser(
        id = "vulpecula-script",
        name = ["vul-script", "vul-ks", "vulscript"]
    )
    fun parser() = scriptParser { reader ->
        parse(reader)
    }
}