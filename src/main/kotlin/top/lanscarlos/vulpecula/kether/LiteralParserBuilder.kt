package top.lanscarlos.vulpecula.kether

import taboolib.library.kether.Parser

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether
 *
 * @author Lanscarlos
 * @since 2023-02-24 23:24
 */
class LiteralParserBuilder : ParserBuilder() {

    val methods = HashMap<String, () -> Parser<Parser.Action<Any?>>>()
    var other: (() -> Parser<Parser.Action<Any?>>)? = null
        private set

    fun case(vararg str: String, func: () -> Parser<Parser.Action<Any?>>) {
        str.forEach { methods[it] = func }
    }

    fun other(func: () -> Parser<Parser.Action<Any?>>) {
        other = func
    }

}