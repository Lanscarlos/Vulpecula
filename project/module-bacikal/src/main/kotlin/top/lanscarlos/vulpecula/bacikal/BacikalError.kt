package top.lanscarlos.vulpecula.bacikal

import taboolib.library.kether.LocalizedException
import java.util.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2024-03-23 13:22
 */
enum class BacikalError {

    SYMBOL_NOT_OPENED, // 符号未打开
    SYMBOL_NOT_CLOSED, // 符号未闭合
    UNKNOWN_ACTION; // 未知语句

    fun create(vararg args: Any?): LocalizedException {
        return LocalizedException.of("bacikal-error." + name.lowercase(Locale.getDefault()).replace("_", "-"), *args)
    }
}