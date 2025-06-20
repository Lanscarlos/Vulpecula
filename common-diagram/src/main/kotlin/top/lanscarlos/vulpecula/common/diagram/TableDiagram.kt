package top.lanscarlos.vulpecula.common.diagram

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

    private inner class Header(val index: Int, val component: ComponentText, val align: Align) {

        val plainText = component.toPlainText()

        val length = plainText.fixedLength()

        var adaptiveWidth: Int = -1

    }

    private inner class Row(val index: Int) {
        val cells = mutableListOf<Cell>()
    }

    private inner class Cell(val header: Header, val row: Row, val component: ComponentText) {

        val plainText = component.toPlainText()

        val length = plainText.fixedLength()

        val align: Align get() = header.align

    }

    private val headers = mutableListOf<Header>() // 表头

    private val rows = mutableListOf<Row>() // 行

    fun addHeader(content: String, align: Align = Align.CENTER) {
        headers += Header(headers.size, Components.text(content), align)
    }

    fun addHeader(component: ComponentText, align: Align = Align.CENTER) {
        headers += Header(headers.size, component, align)
    }

    fun addTextRow(content: List<String>) {
        val row = Row(rows.size)
        for (header in headers) {
            val cell = Cell(header, row, Components.text(content.getOrNull(header.index) ?: ""))
            row.cells += cell
        }
        rows += row

    }

    fun addComponentRow(components: List<ComponentText>) {
        val row = Row(rows.size)
        for (header in headers) {
            val cell = Cell(header, row, components.getOrNull(header.index) ?: Components.text(""))
            row.cells += cell
        }
        rows += row

    }

    fun build(): ComponentText {
        // 计算每一列的自适应宽度
        calculateAdaptiveWidth()

        val lines = mutableListOf<ComponentText>()

        // 绘制表头
        lines += drawHeaders()

        // 绘制数据
        for (row in rows) {
            lines += drawRow(row)
        }

        val builder = Components.empty()
        for (line in lines) {
            builder.newLine().append(line)
        }
        return builder
    }

    private fun drawHeaders(): List<ComponentText> {
        // 渲染上下边框
        val topBorder = "┌" + headers.joinToString(separator = "┬") { "─".repeat(it.adaptiveWidth + padding.times(2)) } + "┐"
        val bottomBorder = "├" + headers.joinToString(separator = "┼") { "─".repeat(it.adaptiveWidth + padding.times(2)) } + "┤"

        val builder = ComponentBuilder()
        for (header in headers) {
            builder.append("│")
            builder.append(" ".repeat(padding))
            when (header.align) {
                Align.LEFT -> {
                    val offset = header.adaptiveWidth - header.length
                    builder.append(header.component)
                    builder.append(" ".repeat(offset))
                }
                Align.RIGHT -> {
                    val offset = header.adaptiveWidth - header.length
                    builder.append(" ".repeat(offset))
                    builder.append(header.component)
                }
                Align.CENTER -> {
                    val offset = (header.adaptiveWidth - header.length) / 2
                    builder.append(" ".repeat(offset))
                    builder.append(header.component)
                    builder.append(" ".repeat(offset))
                    if (header.length % 2 != 0) {
                        builder.append(" ")
                    }
                }
            }
            builder.append(" ".repeat(padding))
        }
        builder.append("│")
        return listOf(
            Components.text(topBorder),
            builder.build(),
            Components.text(bottomBorder)
        )
    }

    private fun drawRow(row: Row): List<ComponentText> {
        // 渲染底部边框
        val border = if (row.index != rows.lastIndex) {
            "├" + headers.joinToString(separator = "┼") { "─".repeat(it.adaptiveWidth + padding.times(2)) } + "┤"
        } else {
            "└" + headers.joinToString(separator = "┴") { "─".repeat(it.adaptiveWidth + padding.times(2)) } + "┘"
        }

        // 渲染数据体
        val builder = ComponentBuilder()
        for (cell in row.cells) {
            val adaptiveWidth = cell.header.adaptiveWidth
            builder.append("│")
            builder.append(" ".repeat(padding))
            if (cell.length == 0) {
                builder.append(" ".repeat(adaptiveWidth))
                builder.append(" ".repeat(padding))
                continue
            }
            when (cell.align) {
                Align.LEFT -> {
                    val offset = adaptiveWidth - cell.length
                    builder.append(cell.component)
                    builder.append(" ".repeat(offset))
                }
                Align.RIGHT -> {
                    val offset = adaptiveWidth - cell.length
                    builder.append(" ".repeat(offset))
                    builder.append(cell.component)
                }
                Align.CENTER -> {
                    val offset = (adaptiveWidth - cell.length) / 2
                    builder.append(" ".repeat(offset))
                    builder.append(cell.component)
                    builder.append(" ".repeat(offset))
                    if (cell.length % 2 != 0) {
                        builder.append(" ")
                    }
                }
            }
            builder.append(" ".repeat(padding))
        }
        builder.append("│")
        return listOf(
            builder.build(),
            Components.text(border)
        )
    }

    private fun calculateAdaptiveWidth() {
        for (header in headers) {
            calculateAdaptiveWidth(header)
        }
    }

    private fun calculateAdaptiveWidth(header: Header) {
        var width = header.length
        for (row in rows) {
            val length = row.cells.getOrNull(header.index)?.length ?: continue
            if (length > width) {
                width = length
            }
        }
        if (width % 2 != 0) {
            width += 1
        }
        header.adaptiveWidth = width
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