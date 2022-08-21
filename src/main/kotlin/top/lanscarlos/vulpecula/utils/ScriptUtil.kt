package top.lanscarlos.vulpecula.utils

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.utils
 *
 * @author Lanscarlos
 * @since 2022-08-19 16:38
 */

internal fun Any.formatToScript(): String? {
    return when (this) {
        is String -> this.formatToScript()
        is Map<*, *> -> {
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
        "ke", "kether" -> "$this\n"
        "js", "javascript" -> "js '$this'\n"
        "ks", "script" -> "vul script run *$this\n"
//            "kf", "fragment" -> "vul fragment inline *$content"
        else -> null
    }
}