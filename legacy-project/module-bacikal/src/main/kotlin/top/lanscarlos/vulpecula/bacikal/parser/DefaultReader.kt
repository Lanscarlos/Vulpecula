package top.lanscarlos.vulpecula.bacikal.parser

import taboolib.common.platform.function.warning
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.module.kether.expects

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2023-08-25 01:02
 */
class DefaultReader(val source: QuestReader) : BacikalReader {

    private val marker: MutableList<Int> = mutableListOf()

    override val index: Int
        get() = source.index

    override fun readToken(): String {
        return source.nextToken()
    }

    override fun readAction(): ParsedAction<*> {
        return if (hasToken("{")) {
            // 语句块
            source.nextParsedAction()
        } else {
            source.nextParsedAction()
        }
    }

    override fun readActionList(): List<ParsedAction<*>> {
        return if (hasToken("[")) {
            mutableListOf<ParsedAction<*>>().also {
                while (!hasToken("]")) {
                    it += readAction()
                }
            }
        } else {
            emptyList()
        }
    }

    override fun peekToken(): String {
        source.mark()
        val token = source.nextToken()
        source.reset()
        return token
    }

    override fun expectToken(vararg expect: String) {
        source.expects(*expect)
    }

    override fun hasToken(vararg expect: String): Boolean {
        source.mark()
        val token = source.nextToken()
        return if (token in expect) {
            true
        } else {
            source.reset()
            false
        }
    }

    override fun mark(): Int {
        source.mark()
        marker += source.mark
        return source.mark
    }

    override fun rollback(offset: Int) {
        if (offset <= 0) {
            return
        }
        if (marker.size <= offset) {
            warning("Out of marker range. ${marker.size} <= $offset.")
            return
        }
        if (offset == 1) {
            // 常规回滚
            source.reset()
            marker.removeLast()
        } else {
            // 回滚到指定位置
            val index = marker[marker.lastIndex - offset]
            try {
                source.setProperty("index", index)
                // 移除多余的标记
                repeat(offset) {
                    marker.removeLast()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                warning("Failed to rollback to index $index.")
            }
        }
    }
}