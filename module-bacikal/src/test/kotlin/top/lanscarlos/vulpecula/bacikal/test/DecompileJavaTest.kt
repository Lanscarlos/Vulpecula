package top.lanscarlos.vulpecula.bacikal.test

import org.junit.jupiter.api.Test

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.test
 *
 * @author Lanscarlos
 * @since 2024-11-20 11:58
 */
class DecompileJavaTest {

    class Action {

        fun resolve(
            name: String?,
            offset: Int,
        ): String {
            return "Parser: $name, offset: $offset"
        }

    }


    @Test
    fun test() {
        val clazz = Action::class.java
        val methods = clazz.declaredMethods
        for (method in methods) {
            println()
            println("Method: ${method.name}")
            val parameters = method.parameters
            for (parameter in parameters) {
                println("Parameter: ${parameter.name}; Type: ${parameter.type}; modifiers: ${parameter.modifiers}")
            }
            println()
        }

        println()
        val method = methods.find { it.name == "resolve\$default" || it.name == "resolve" } ?: error("Method resolve not found")
        val parameters = method.parameters
        for (parameter in parameters) {
            println("Parameter: ${parameter.name}; Type: ${parameter.type}; modifiers: ${parameter.modifiers}")
        }
    }

}