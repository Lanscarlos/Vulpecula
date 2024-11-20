package top.lanscarlos.vulpecula.bacikal.parser

import taboolib.library.kether.ParsedAction

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-20 21:30
 */
interface BacikalReader {

    val index: Int

    /**
     * 读取一个标记
     * */
    fun readToken(): String

    fun readAction(): ParsedAction<*>

    fun readActionList(): List<ParsedAction<*>>

    /**
     * 查看下一个标记，但不改变位置
     * */
    fun peekToken(): String

    /**
     * 读取下一个标记并判断是否符合预期，若不符合预期则抛出异常
     * */
    fun expectToken(vararg expect: String)

    /**
     * 读取一个标记并判断是否符合预期
     * 若不符合预期则重置位置
     * */
    fun hasToken(vararg expect: String): Boolean

    /**
     * 标记位置
     * */
    fun mark(): Int

    /**
     * 回滚到标记位置
     * */
    fun rollback(offset: Int = 1)

}