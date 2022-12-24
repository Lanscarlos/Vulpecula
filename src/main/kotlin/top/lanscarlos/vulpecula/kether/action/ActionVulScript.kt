package top.lanscarlos.vulpecula.kether.action

import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.readString
import top.lanscarlos.vulpecula.kether.live.tryReadEntity
import top.lanscarlos.vulpecula.script.VulScript
import top.lanscarlos.vulpecula.script.VulWorkspace
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
object ActionVulScript {

    /**
     * vul-script run $file by $viewer -async with [ args... ]
     *
     * vul-script stop *
     * vul-script stop $file
     *
     * vul-script compile/build *
     * vul-script compile/build $file
     * */
    @VulKetherParser(
        id = "vul-script",
        name = ["vul-script", "vul-ks", "vulscript"]
    )
    fun parser() = scriptParser { reader ->
        reader.switch {
            case("run") {
                val file = reader.readString()
                val viewer = reader.tryReadEntity("by")
                val async = reader.hasNextToken("-async")
                val args = reader.tryNextActionList("with")

                actionTake {
                    val future = CompletableFuture<Any?>()

                    listOf(
                        file.getOrNull(this),
                        viewer?.getOrNull(this),
                        *(args ?: emptyList()).map { this.run(it) }.toTypedArray()
                    ).thenTake().thenAccept { args ->
                        val id = args[0]?.toString() ?: error("No script file found: \"${args[0]}\"")
                        val result = if (args.size > 2) {
                            // 有参数
                            VulWorkspace.runScript(id, args[1], *args.subList(2, args.size).toTypedArray())
                        } else {
                            // 无参数
                            VulWorkspace.runScript(id, args[1])
                        }

                        if (async) {
                            future.complete(null)
                        } else {
                            result?.thenAccept { future.complete(it) } ?: future.complete(null)
                        }
                    }

                    return@actionTake future
                }
            }

            case("stop") {
                if (reader.hasNextToken("*")) {
                    actionNow {
                        try {
                            VulWorkspace.terminateAllScript()
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
                                VulWorkspace.terminateScript(it ?: return@thenApply false)
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
}