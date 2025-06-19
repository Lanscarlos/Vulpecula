package top.lanscarlos.vulpecula.common.diagram

import taboolib.common.platform.function.info
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.diagram
 *
 * @author Lanscarlos
 * @since 2025/6/19
 */
class TableDiagram(val padding: Int = 2) {

    enum class Align {
        LEFT, CENTER, RIGHT
    }

    class Header(val index: Int, val content: String)

    class Row(val index: Int, val content: List<Data>)

    class Data(val index: Int, val content: String)

    val align: Align = Align.CENTER

    val headers = mutableListOf<Header>() // 表头

    val rows = mutableListOf<Row>() // 行

    fun addHeader(content: String) {
        headers += Header(headers.size, content)
    }

    fun addRow(content: List<String>) {
        rows += Row(rows.size, content.mapIndexed(::Data))
    }

    fun build(): ComponentText {
        // 计算每一列的自适应宽度
        val adaptiveWidth = headers.map(::calculateAdaptiveWidth)

        val lines = mutableListOf<ComponentText>()

        // 绘制表头
        lines += drawHeaders(adaptiveWidth)

        // 绘制数据
        for (row in rows) {
            lines += drawRow(row, adaptiveWidth)
        }

        val builder = Components.empty()
        for (line in lines) {
            builder.newLine().append(line)
        }
        return builder
    }

    fun drawHeaders(adaptiveWidth: List<Int>): List<ComponentText> {
        val top = Components.empty()
        val body = Components.empty()
        val bottom = Components.empty()

        for ((index, header) in headers.withIndex()) {
            val width = adaptiveWidth[index]
            if (index == 0) {
                top.append("┌")
                body.append("│")
                bottom.append("├")
            }
            top.append("─".repeat(width + padding * 2))
            bottom.append("─".repeat(width + padding * 2))
            if (index == headers.lastIndex) {
                top.append("┐")
                bottom.append("┤")
            } else {
                top.append("┬")
                bottom.append("┼")
            }

            when (align) {
                Align.LEFT -> {
                    body.append(" ".repeat(padding))
                    body.append(header.content)
                    val offset = width - header.content.fixedLength()
                    body.append(" ".repeat(padding + offset))
                }
                Align.RIGHT -> {
                    val offset = width - header.content.fixedLength()
                    body.append(" ".repeat(padding + offset))
                    body.append(header.content)
                    body.append(" ".repeat(padding))
                }
                Align.CENTER -> {
                    val length = header.content.fixedLength()
                    if (length % 2 == 0) {
                        // 双数长度
                        val offset = (width - length) / 2
                        val padding = this.padding + offset
                        body.append(" ".repeat(padding))
                        body.append(header.content)
                        body.append(" ".repeat(padding))
                    }
                }
            }
            body.append("│")
        }

        return listOf(top, body, bottom)
    }

    fun drawRow(row: Row, adaptiveWidth: List<Int>): List<ComponentText> {
        val body = Components.empty()
        val bottom = Components.empty()

        for ((index, column) in row.content.withIndex()) {
            val width = adaptiveWidth[index]
            if (index == 0) {
                body.append("│")
                bottom.append(if (row.index != rows.lastIndex) "├" else "└")
            }
            bottom.append("─".repeat(width + padding * 2))
            if (index == headers.lastIndex) {
                bottom.append(if (row.index != rows.lastIndex) "┤" else "┘")
            } else {
                bottom.append(if (row.index != rows.lastIndex) "┼" else "┴")
            }

            when (align) {
                Align.LEFT -> {
                    body.append(" ".repeat(padding))
                    body.append(column.content)
                    val offset = width - column.content.fixedLength()
                    body.append(" ".repeat(padding + offset))
                }
                Align.RIGHT -> {
                    val offset = width - column.content.fixedLength()
                    body.append(" ".repeat(padding + offset))
                    body.append(column.content)
                    body.append(" ".repeat(padding))
                }
                Align.CENTER -> {
                    val length = column.content.fixedLength()
                    val offset = (width - length) / 2
                    if (length % 2 == 0) {
                        // 双数长度
                        val padding = this.padding + offset
                        body.append(" ".repeat(padding))
                        body.append(column.content)
                        body.append(" ".repeat(padding))
                    } else {
                        val padding = this.padding + offset
                        body.append(" ".repeat(padding))
                        body.append(column.content)
                        body.append(" ".repeat(padding + 1))
                    }
                }
            }
            body.append("│")
        }

        return listOf(body, bottom)
    }

    fun calculateAdaptiveWidth(header: Header): Int {
        var width = header.content.fixedLength()
        for (row in rows) {
            val length = row.content.getOrNull(header.index)?.content?.fixedLength() ?: continue
            if (length > width) {
                width = length
            }
        }
        if (width % 2 != 0) {
            width += 1
        }
        info("header ${header.content} >> $width")
        return width
    }

    private fun String.fixedLength(): Int {
        return sumOf { it.fixedLength() }
    }

    private fun Char.fixedLength(): Int {
        return when (this) {
            '_', '.', '-',
            in 'A'..'Z',
            in 'a'..'z',
            in '0'..'9' -> 1
            else -> 2
        }
    }

}