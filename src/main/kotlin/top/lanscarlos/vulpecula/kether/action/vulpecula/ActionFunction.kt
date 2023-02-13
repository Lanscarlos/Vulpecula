package top.lanscarlos.vulpecula.kether.action.vulpecula

import taboolib.module.kether.actionTake
import taboolib.module.kether.run
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.thenTake
import top.lanscarlos.vulpecula.utils.tryNextActionList
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal.script
 *
 * @author Lanscarlos
 * @since 2022-12-23 21:24
 */
object ActionFunction {

    /**
     * func xxx
     * func xxx with [ arg0 arg1 ]
     * */
    @VulKetherParser(
        id = "func",
        name = ["func"],
        namespace = "vulpecula-script"
    )
    fun parser() = scriptParser { reader ->
        val name = reader.nextToken()
        val args = reader.tryNextActionList("with")

        actionTake {
            val function = this.context().quest.blocks["function_$name"] ?: error("function \"$name\" not found")
            val newFrame = newFrame(name)
            newFrame.setNext(function)
            addClosable(newFrame)

            if (args != null) {
                val future = CompletableFuture<Any?>()

                args.map { this.run(it) }.thenTake().thenAccept { values ->
                    for ((i, arg) in values.withIndex()) {
                        newFrame.variables().set("arg${i}", arg)
                    }
                    newFrame.variables()["args"] = values

                    newFrame.run<Any?>().thenAccept {
                        future.complete(it)
                    }
                }

                return@actionTake future
            } else {
                return@actionTake newFrame.run<Any?>()
            }
        }
    }
}