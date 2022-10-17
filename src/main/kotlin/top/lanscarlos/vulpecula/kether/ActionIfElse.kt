package top.lanscarlos.vulpecula.kether

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import taboolib.module.kether.action.transform.CheckType
import top.lanscarlos.vulpecula.utils.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether
 *
 * @author Lanscarlos
 * @since 2022-10-17 15:56
 */
object ActionIfElse {

    interface Condition {
        fun run(frame: ScriptFrame): Boolean
    }

    class SingleCondition(
        val condition: ParsedAction<*>
    ) : Condition {
        override fun run(frame: ScriptFrame): Boolean {
            return condition.run(frame).toBoolean(false)
        }
    }

    class LogicCondition(
        val left: ParsedAction<*>,
        val checkType: CheckType,
        val right: ParsedAction<*>,
    ) : Condition {
        override fun run(frame: ScriptFrame): Boolean {
            return checkType.check(left.run(frame), right.run(frame))
        }
    }

    fun QuestReader.nextCondition(): Condition {
        val origin = this.nextParsedAction()
        this.mark()
        val token = this.nextToken()
        val checkType = if (!LogicSymbol.isLogicSymbol(token)) {
            CheckType.fromStringSafely(token)
        } else null

        return if (checkType != null) {
            val next = this.nextParsedAction()
            LogicCondition(origin, checkType, next)
        } else {
            this.reset()
            return SingleCondition(origin)
        }
    }

    /**
     * 解析条件语句块
     * @return 逆波兰表达式
     * */
    fun QuestReader.parseCondition(): List<Any> {
        val reader = this
        val expression = ArrayList<Any>()
        val stack = Stack<LogicSymbol>()

        reader.mark()
        var symbol = LogicSymbol.fromString(reader.nextToken()) ?: let {
            // 非逻辑符号，先加载条件
            reader.reset()
            expression.add(reader.nextCondition())
            reader.mark()
            LogicSymbol.fromString(reader.nextToken())
        }
        while (symbol != null) {
            when (symbol) {
                // 左括号，入栈
                LogicSymbol.BRACKETS_LEFT -> {
                    stack.push(symbol)
                    // 尝试存入下一个元素
                    if (!LogicSymbol.isLogicSymbol(reader.nextPeek())) {
                        expression.add(reader.nextCondition())
                    }
                }
                // 右括号
                LogicSymbol.BRACKETS_RIGHT -> {
                    // 栈不为空，符号出栈，加入表达式
                    while (stack.isNotEmpty() && stack.peek() != LogicSymbol.BRACKETS_LEFT) {
                        expression.add(stack.pop())
                    }
                    // 退出左括号
                    stack.pop()
                }
                else -> {
                    if (stack.empty() || stack.peek() == LogicSymbol.BRACKETS_LEFT || symbol.priority > stack.peek().priority) {
                        // 栈空 或 栈顶为左括号 或 当前运算符优先级高
                        // 运算符入栈
                        stack.push(symbol)
                        // 尝试存入下一个元素
                        if (!LogicSymbol.isLogicSymbol(reader.nextPeek())) {
                            expression.add(reader.nextCondition())
                        }
                    } else {
                        // 当前符号优先级低或同级
                        while (stack.isNotEmpty() && stack.peek().priority >= symbol.priority) {
                            // 将栈内运算符依次取出，直至栈空 或 栈顶运算符优先级低于当前运算符
                            expression.add(stack.pop())
                        }
                        stack.push(symbol)
                        // 尝试存入下一个元素
                        if (!LogicSymbol.isLogicSymbol(reader.nextPeek())) {
                            expression.add(reader.nextCondition())
                        }
                    }
                }
            }

            reader.mark()
            symbol = LogicSymbol.fromString(reader.nextToken())
        }

        reader.reset()

        // 将栈内剩余符号取出
        while (stack.isNotEmpty()) {
            expression.add(stack.pop())
        }

        return expression
    }

    enum class LogicSymbol(val priority: Int) {

        BRACKETS_LEFT(3),
        BRACKETS_RIGHT(3),
//        NOT(2),
        AND(1),
        OR(0);

        companion object {

            fun isLogicSymbol(symbol: String): Boolean {
                return fromString(symbol) != null
            }

            fun fromString(string: String): LogicSymbol? {
                return when (string) {
                    "(" -> BRACKETS_LEFT
                    ")" -> BRACKETS_RIGHT
                    "&&", "and" -> AND
                    "||", "or" -> OR
//                    "!!", "!", "not" -> NOT
                    else -> null
                }
            }
        }
    }

    /**
     * 拓展 If 语句
     *
     * if* {...} {...} else {...}
     * if* {...} then {...} else {...}
     * if* {...} {symbol} {...} then {...}
     * if* {...} and/or {...} then {...}
     * */
    @KetherParser(["if*"], shared = true)
    fun parse() = scriptParser { reader ->
        val expression = reader.parseCondition()
        val trueBlock = reader.tryNextBlock("then") ?: reader.nextBlock()
        val falseBlock = reader.tryNextBlock("else")
        actionNow {

            // 计算逆波兰表达式
            val stack = Stack<Boolean>()
            val queue = expression.toMutableList()
            while (queue.isNotEmpty()) {
                when (val it = queue.removeFirst()) {
                    is Condition -> {
                        stack.push(it.run(this))
                    }
                    LogicSymbol.AND -> {
                        val first = stack.pop()
                        val second = stack.pop()
                        stack.push(first && second)
                    }
                    LogicSymbol.OR -> {
                        val first = stack.pop()
                        val second = stack.pop()
                        stack.push(first || second)
                    }
                }
            }

            return@actionNow if (stack.pop()) {
                trueBlock.run(this)
            } else falseBlock?.run(this)
        }
    }
}