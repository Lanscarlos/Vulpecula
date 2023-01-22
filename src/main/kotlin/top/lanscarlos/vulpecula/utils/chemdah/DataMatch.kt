package top.lanscarlos.vulpecula.utils

import taboolib.module.kether.action.transform.CheckType
import taboolib.module.kether.inferType

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.selector.DataMatch
 *
 * @author 坏黑
 * @since 2022/10/27 09:33
 */
data class DataMatch(val key: String, val value: Any, val type: CheckType) {

    fun check(target: Any, type: CheckType = this.type): Boolean {
        return type.check(value, target)
    }

    companion object {

        /**
         * Match data
         *
         * EQ:
         * data=1
         * NE:
         * data!=1
         * GT:
         * data>1
         * LT:
         * data<1
         * GE:
         * data>=1
         * LE:
         * data<=1
         * CONTAINS:
         * data(1|2|3)
         */
        fun fromString(str: String): DataMatch {
            val chars = str.toCharArray()
            var index = 0
            var type: CheckType? = null
            for (i in chars.indices) {
                if (type == null) {
                    when (chars[i]) {
                        '!' -> {
                            index = i
                            type = CheckType.EQUALS_NOT
                        }
                        '=' -> {
                            index = i
                            type = CheckType.EQUALS
                        }
                        '>' -> {
                            index = i
                            type = if (i + 1 < chars.size && chars[i + 1] == '=') {
                                CheckType.GTE
                            } else {
                                CheckType.GT
                            }
                        }
                        '<' -> {
                            index = i
                            type = if (i + 1 < chars.size && chars[i + 1] == '=') {
                                CheckType.LTE
                            } else {
                                CheckType.LT
                            }
                        }
                        '(' -> {
                            index = i
                            type = CheckType.CONTAINS
                        }
                    }
                }
            }
            type ?: error("Invalid data match: $str")
            // 获取 key
            val key = str.substring(0, index)
            // 获取 value
            val value: Any = when (type) {
                // 两个符号的表达式
                CheckType.EQUALS_NOT, CheckType.GTE, CheckType.LTE -> str.substring(index + 2)
                // 包含表达式
                CheckType.CONTAINS -> {
                    val array = str.substring(index + 1, str.length - 1).split("[/|]".toRegex()).map { it.inferType()!! }
                    // 如果列表中只有一个元素
                    if (array.size == 1) {
                        type = CheckType.IN
                        array[0]
                    } else {
                        array
                    }
                }
                else -> str.substring(index + 1)
            }
            return DataMatch(key, value, type)
        }
    }
}