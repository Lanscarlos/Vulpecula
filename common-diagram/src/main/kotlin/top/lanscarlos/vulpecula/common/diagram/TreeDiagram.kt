package top.lanscarlos.vulpecula.common.diagram

import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.chat.StandardColors
import java.util.function.BiFunction
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.diagram
 *
 * @author Lanscarlos
 * @since 2025/6/19
 */
class TreeDiagram<T>(
    val tabBranch: String = TAB_BRANCH + TAB_CONNECTOR_HORIZONTAL.repeat(2) + TAB_SPACE,
    val tabBranchEnd: String = TAB_BRANCH_END + TAB_CONNECTOR_HORIZONTAL.repeat(2) + TAB_SPACE,
    val tabIndicator: String = TAB_CONNECTOR_VERTICAL + TAB_SPACE.repeat(3),
    val tabEmpty: String = TAB_SPACE.repeat(4)
) {

    companion object {
        const val TAB_SPACE = " " // 空白符
        const val TAB_CONNECTOR_HORIZONTAL = "─" // 水平链接线
        const val TAB_CONNECTOR_VERTICAL = "│" // 垂直连接线
        const val TAB_BRANCH = "├" // 分支
        const val TAB_BRANCH_END = "└" // 末端分支
    }

    private lateinit var onDrawHandler: Function<T, ComponentText>

    private lateinit var onIndentHandler: Function<T, Int>

    private lateinit var onTraversalHandler: BiFunction<Int, T, List<T>> // 层级, 遍历对象 -> 子对象列表

    fun onDraw(func: Function<T, ComponentText>) {
        this.onDrawHandler = func
    }

    fun onIndent(func: Function<T, Int>) {
        this.onIndentHandler = func
    }

    fun onTraversal(func: BiFunction<Int, T, List<T>>) {
        this.onTraversalHandler = func
    }

    fun build(root: T): ComponentText {
        val builder = Components.empty()
        for (component in buildList(root)) {
            builder.newLine().append(component)
        }
        return builder
    }

    fun buildList(root: T): List<ComponentText> {
        require(::onDrawHandler.isInitialized)
        require(::onIndentHandler.isInitialized)
        require(::onTraversalHandler.isInitialized)
        return draw(0, root)
    }

    private fun draw(depth: Int, node: T): List<ComponentText> {
        val lines = mutableListOf<ComponentText>()

        // 绘制当前节点
        lines += onDrawHandler.apply(node)
            .resetColor()

        // 尝试遍历子节点并绘制
        val children = onTraversalHandler.apply(depth, node)
        if (children.isEmpty()) {
            return lines
        }
        val indent = onIndentHandler.apply(node).coerceAtLeast(0).let(" "::repeat)
        for ((index, child) in children.withIndex()) {
            val header = if (index != children.lastIndex) tabBranch else tabBranchEnd
            val body = if (index == children.lastIndex) tabEmpty else tabIndicator

            // 获取子节点的绘图
            val components = draw(depth + 1, child)
            for ((index, component) in components.withIndex()) {
                lines += if (index == 0) {
                    Components.text(indent)
                        .append(header)
                        .append(component)
                        .resetColor()
                } else {
                    Components.text(indent)
                        .append(body)
                        .append(component)
                        .resetColor()
                }
            }
        }

        return lines
    }

    private fun ComponentText.resetColor(): ComponentText {
        return append(Components.text("").color(StandardColors.RESET))
    }

}