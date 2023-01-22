package top.lanscarlos.vulpecula.utils.chemdah

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.utils.chemdah
 *
 * @author Lanscarlos
 * @since 2023-01-23 00:24
 */
fun String.startsWithAny(vararg prefix: String): Boolean {
    return prefix.any { startsWith(it) }
}

fun String.substringAfterAny(vararg morePrefix: String): String {
    return substringAfter(morePrefix.firstOrNull { startsWithAny(it) } ?: return this)
}