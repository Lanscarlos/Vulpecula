package top.lanscarlos.vulpecula.utils

import taboolib.common.platform.function.info
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.kether.Script
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.parseKetherScript
import top.lanscarlos.vulpecula.internal.ScriptBuilder
import top.lanscarlos.vulpecula.internal.ScriptFragment
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.utils
 *
 * @author Lanscarlos
 * @since 2022-08-19 16:38
 */


//private val scriptCache = ConcurrentHashMap<String, Script>()

val defNameSpace = "vulpecula"

fun Script.runActions(func: ScriptContext.() -> Unit): CompletableFuture<Any?> {
    return ScriptContext.create(this).apply(func).runActions()
}

fun String.parseToScript(namespace: List<String>): Script {
    val source = if (this.startsWith("def ")) this else "def main = { $this }"
    return if (defNameSpace !in namespace) {
        ScriptBuilder(source).build().parseKetherScript(namespace.plus(defNameSpace))
    } else {
        ScriptBuilder(source).build().parseKetherScript(namespace)
    }
}

internal fun Any.formatToScript(): String? {
    info("formatToScript -> ${this::class.java.name}")
    return when (this) {
        is String -> this.formatToScript()
        is Map<*, *> -> {
            val content = this["content"]?.toString() ?: return null
            val type = this["type"]?.toString()
            content.formatToScript(type ?: "ke")
        }
        is ConfigurationSection -> {
            val content = this["content"]?.toString() ?: return null
            val type = this["type"]?.toString()
            content.formatToScript(type ?: "ke")
        }
        is List<*> -> {
            val sb = StringBuilder()
            this.forEach {

                if (it is String) {
                    sb.append(it.formatToScript())
                    return@forEach
                }

                val meta = it as? Map<*, *> ?: return@forEach
                val content = meta["content"]?.toString() ?: return@forEach
                val type = meta["type"]?.toString()
                sb.append(content.formatToScript(type ?: "ke"))
            }
            if (sb.isNotEmpty()) sb.toString() else null
        }
        else -> null
    }
}

private fun String.formatToScript(type: String = "ke"): String? {
    return when (type.lowercase()) {
        "ke", "kether" -> this + '\n'
        "js", "javascript" -> "js '$this'\n"
        "ks", "script" -> "vul script run *$this\n"
        "kf", "fragment" -> ScriptFragment.get(this)?.let { it + '\n' }
        else -> null
    }
}