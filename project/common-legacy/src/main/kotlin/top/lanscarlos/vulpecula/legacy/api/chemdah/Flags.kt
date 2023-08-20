package top.lanscarlos.vulpecula.legacy.api.chemdah

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey

enum class Flags(val match: (String, String) -> Boolean) {

    DEFAULT({ name, value -> name == value }),

    STARTS_WITH({ name, value -> name.startsWith(value) }),

    ENDS_WITH({ name, value -> name.endsWith(value) }),

    CONTAINS({ name, value -> name.contains(value) }),

    TAG({ name, value -> value.asTags().any { it.name.equals(name, true) } }),

    ALL({ _, _ -> true });

    companion object {

        private val tagsMap = HashMap<String, Set<Material>>()

        fun String.asTags(): Set<Material> {
            return tagsMap.computeIfAbsent(this) {
                val set = HashSet<Material>()
                set.addAll(Bukkit.getTag("blocks", NamespacedKey.minecraft(this), Material::class.java)?.values ?: emptySet())
                set.addAll(Bukkit.getTag("items", NamespacedKey.minecraft(this), Material::class.java)?.values ?: emptySet())
                set
            }
        }

        fun String.matchType(flags: MutableList<Flags>): String {
            var result = this
            if (result == "*") {
                flags.add(ALL)
            } else if (result.startsWith('%') && result.endsWith('%')) {
                result = result.substring(1, result.length - 1)
                flags.add(TAG)
            } else {
                if (result.startsWith('(') && result.endsWith(')')) {
                    result = result.substring(1, result.length - 1)
                    flags.add(CONTAINS)
                }
                if (result.startsWith('^')) {
                    result = result.substring(1)
                    flags.add(STARTS_WITH)
                }
                if (result.endsWith('$')) {
                    result = result.substring(0, result.length - 1)
                    flags.add(ENDS_WITH)
                }
            }
            flags.add(DEFAULT)
            return result
        }
    }
}
