package top.lanscarlos.vulpecula.utils

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.kether.Script
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.parseKetherScript
import top.lanscarlos.vulpecula.internal.ScriptBuilder
import top.lanscarlos.vulpecula.internal.ScriptFragment
import java.util.concurrent.CompletableFuture

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

internal fun Any.formatToScript(ignoreCondition: Boolean = false): String? {
    return when (this) {
        is String -> this.formatToScript()
        is Map<*, *> -> {
            val content = this["content"]?.toString() ?: return null
            val type = this["type"]?.toString()
            if (ignoreCondition) {
                content.formatToScript(type ?: "ke")
            } else {
                val condition = this["condition"]?.formatToScript(true)
                val deny = this["deny"]?.formatToScript(true)
                content.formatToScript(type ?: "ke", condition, deny)
            }
        }
        is ConfigurationSection -> {
            val content = this.getString("content") ?: return null
            val type = this.getString("type")
            if (ignoreCondition) {
                content.formatToScript(type ?: "ke")
            } else {
                val condition = this["condition"]?.formatToScript(true)
                val deny = this["deny"]?.formatToScript(true)
                content.formatToScript(type ?: "ke", condition, deny)
            }
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
                if (ignoreCondition) {
                    content.formatToScript(type ?: "ke")
                } else {
                    val condition = meta["condition"]?.formatToScript(true)
                    val deny = meta["deny"]?.formatToScript(true)
                    content.formatToScript(type ?: "ke", condition, deny)
                }
            }
            if (sb.isNotEmpty()) sb.toString() else null
        }
        else -> null
    }
}

private fun String.formatToScript(type: String = "ke", condition: String? = null, deny: String? = null): String? {
    val sb = StringBuilder()

    // 加载条件
    condition?.let {
        sb.append("if {\n$it} then {\n")
    }

    val content = when (type.lowercase()) {
        "ke", "kether" -> this + '\n'
        "js", "javascript" -> "js '$this'\n"
        "ks", "script" -> "vul script run *$this\n"
        "kf", "fragment" -> ScriptFragment.get(this)?.let { it + '\n' }
        else -> null
    }

    sb.append(content)

    // 加载条件后续
    condition?.let { _ ->
        sb.append("}")
        deny?.let {
            sb.append(" else {\n$it}")
        }
        sb.append("\n")
    }

    return if (sb.isNotEmpty()) sb.toString() else null
}