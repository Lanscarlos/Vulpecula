package top.lanscarlos.vulpecula.module.bacikal.parser

import taboolib.common.reflect.hasAnnotation
import taboolib.library.kether.ParsedAction
import top.lanscarlos.vulpecula.common.applicative.ApplicativeRegistry
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.Expected
import top.lanscarlos.vulpecula.module.bacikal.annotation.Optional
import java.lang.reflect.Parameter

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2025/6/28
 */
class ClassActionParameter(
    val index: Int,
    val name: String,
    val isNullable: Boolean,
    val hasDefaultValue: Boolean,
    source: Parameter
) {

    /**
     * 参数类型
     * */
    val type: Class<*> = source.type

    /**
     * 参数前缀
     * */
    val prefix: List<String>

    /**
     * 参数修饰符
     * */
    val modifier: Modifier

    init {
        // 解析参数修饰符和前缀
        when {
            source.hasAnnotation(Expected::class.java) -> {
                modifier = Modifier.EXPECTED
                prefix = source.getAnnotation(Expected::class.java).values.toList()
            }
            source.hasAnnotation(Optional::class.java) -> {
                modifier = Modifier.OPTIONAL
                prefix = source.getAnnotation(Optional::class.java).values.toList()
            }
            source.hasAnnotation(Additional::class.java) -> {
                modifier = Modifier.ADDITIONAL
                prefix = source.getAnnotation(Additional::class.java).values.toList()
            }
            else -> {
                modifier = Modifier.NONE
                prefix = emptyList<String>()
            }
        }

        // 检查前缀
        if (modifier != Modifier.NONE) {
            require(prefix.isNotEmpty()) { "前缀不能为空" }
            require(prefix.all { it.toIntOrNull() == null }) { "修饰符前缀不能为数字" }
        }
    }

    fun read(reader: BacikalReader): BacikalAction<*> {
        if (type == BacikalFrame::class.java) {
            return FrameAction
        }
        val action: ParsedAction<*> = when (modifier) {
            Modifier.NONE,
            Modifier.ADDITIONAL -> {
                // 附加参数前面前缀在本函数调用前已经验证过了
                reader.readAction()
            }
            Modifier.EXPECTED -> {
                reader.expectToken(prefix)
                reader.readAction()
            }
            Modifier.OPTIONAL -> {
                if (!reader.hasToken(prefix)) {
                    return DefaultAction(index, type)
                }
                reader.readAction()
            }
        }

        val applicative = ApplicativeRegistry.getApplicative(type)
        return ApplicativeAction(action, applicative)
    }

    /**
     * 语句参数修饰符
     * */
    enum class Modifier {
        NONE, EXPECTED, OPTIONAL, ADDITIONAL
    }

}