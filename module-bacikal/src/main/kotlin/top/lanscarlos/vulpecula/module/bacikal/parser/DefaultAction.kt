package top.lanscarlos.vulpecula.module.bacikal.parser

import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * 缺省参数
 *
 * @param index 缺省位置
 *
 * @author Lanscarlos
 * @since 2025/6/28
 */
class DefaultAction(index: Int, type: Class<*>) : BacikalAction<Any?> {

    // 计算缺省掩码值
    val mask: Int = 1 shl index

    // 对基本类型生成缺省值
    val defaultValue = when (type) {
        Boolean::class.java -> false
        Short::class.java -> 0.toShort()
        Int::class.java -> 0
        Long::class.java -> 0L
        Float::class.java -> 0.0f
        Double::class.java -> 0.0
        else -> null
    }

    override fun execute(frame: BacikalFrame): CompletableFuture<Any?> {
        return CompletableFuture.completedFuture(defaultValue)
    }
}