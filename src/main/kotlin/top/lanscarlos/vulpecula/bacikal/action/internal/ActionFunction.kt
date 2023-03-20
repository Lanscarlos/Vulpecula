package top.lanscarlos.vulpecula.bacikal.action.internal

import top.lanscarlos.vulpecula.bacikal.bacikal
import top.lanscarlos.vulpecula.bacikal.BacikalParser

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.internal
 *
 * @author Lanscarlos
 * @since 2023-03-19 21:09
 */
object ActionFunction {
    /**
     * func xxx
     * func xxx with [ arg0 arg1 ]
     * */
    @BacikalParser(
        id = "func",
        name = ["func"],
        namespace = "vulpecula-script"
    )
    fun parser() = bacikal {
        combineOf(
            text(),
            optional("with", "using", then = list())
        ) { name, args ->
            val function = this.context().quest.blocks["function_$name"] ?: error("function \"$name\" not found")
            val newFrame = this.newFrame(name)
            newFrame.setNext(function)
            this.addClosable(newFrame)

            if (args != null) {
                for ((i, arg) in args.withIndex()) {
                    newFrame.variables().set("arg$i", arg)
                }
                newFrame.variables()["args"] = args
            }

            newFrame.run<Any?>()
        }
    }
}