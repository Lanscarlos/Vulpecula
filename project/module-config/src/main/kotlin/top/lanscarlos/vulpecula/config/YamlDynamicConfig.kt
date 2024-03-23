package top.lanscarlos.vulpecula.config

import taboolib.module.configuration.Configuration
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2024-03-23 19:15
 */
class YamlDynamicConfig(file: File, config: Configuration) : AbstractDynamicConfig(file, config) {

    constructor(file: File) : this(file, Configuration.loadFromFile(file))

    override fun indexOf(path: String): Int {
        if (path.isEmpty() || !file.exists()) {
            return -1
        }

        return try {
            val content = file.readLines()
            val target = path.split('.').toMutableList()
            var line = 0
            var layer = 0
            val node = StringBuilder()
            outer@while (line < content.size && target.isNotEmpty()) {

                val current = content[line]
                var index = 0

                // 过滤空行
                if (content[line].isBlank()) {
                    continue@outer
                }

                // 获取缩进层级
                var indent = 0
                inner@while (index < current.length && current[index].isWhitespace()) {
                    indent++
                    index++
                }

                // 判断缩进是否处于当前层级, 同时过滤块节点内容
                if (layer * 2 != indent) {

                    if (layer > 0 && indent < layer * 2) {
                        // 层级不为零 而且当前缩进比层级小 说明当前已脱离对应匹配的层级了 直接结束
                        return -1
                    }

                    continue@outer
                }

                // 获取节点
                var hasQuotation = false // 节点是否有引号
                node.clear()
                inner@while (index < current.length) {
                    when (val char = current[index]) {
                        '\'', '\"' -> {
                            if (hasQuotation) {
                                // 引号结束
                                break@inner
                            } else {
                                // 引号开头
                                hasQuotation = true
                            }
                        }
                        '#' -> {
                            if (hasQuotation) {
                                // 属于引号内容
                                node.append(char)
                            } else {
                                // 注释
                                break@inner
                            }
                        }
                        ':' -> {
                            if (hasQuotation) {
                                // 属于引号内容
                                node.append(char)
                            } else {
                                // 得到节点名
                                break@inner
                            }
                        }
                        else -> {
                            if (char.isWhitespace() && !hasQuotation) {
                                // 不属于引号内的空白符
                                break@inner
                            } else {
                                node.append(char)
                            }
                        }
                    }
                    index++
                }

                // 节点名检测
                if (node.isBlank()) {
                    // 节点名为空
                    continue@outer
                }

                if (node.toString() == target.first()) {
                    // 节点名匹配 层级缩进增加
                    layer++
                    target.removeFirst()
                }

                line++
            }

            if (target.isEmpty()) {
                // 匹配成功
                line
            } else {
                // 匹配失败
                -1
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            -1
        }
    }

}