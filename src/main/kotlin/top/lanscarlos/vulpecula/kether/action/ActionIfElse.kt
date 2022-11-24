package top.lanscarlos.vulpecula.kether.action

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import taboolib.module.kether.action.transform.CheckType
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.*
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-10-17 15:56
 */
class ActionIfElse(
    val expression: List<Any>,
    val trueBlock: ParsedAction<*>,
    val falseBlock: ParsedAction<*>?
) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {

        // 计算逆波兰表达式
        val stack = Stack<CompletableFuture<Boolean>>()
        val queue = expression.toMutableList()
        while (queue.isNotEmpty()) {
            when (val it = queue.removeFirst()) {
                is Condition -> {
                    stack.push(it.run(frame))
                }
                LogicSymbol.AND -> {
                    val first = stack.pop()
                    val second = stack.pop()
                    val future = listOf(first, second).thenTake().thenApply {
                        it[0].toBoolean(false) && it[1].toBoolean(false)
                    }
                    stack.push(future)
                }
                LogicSymbol.OR -> {
                    val first = stack.pop()
                    val second = stack.pop()
                    val future = listOf(first, second).thenTake().thenApply {
                        it[0].toBoolean(false) || it[1].toBoolean(false)
                    }
                    stack.push(future)
                }
            }
        }

        val future = CompletableFuture<Any?>()
        stack.pop().thenApply { result ->
            if (result) {
                frame.run(trueBlock).thenApply { future.complete(it) }
            } else if (falseBlock != null) {
                frame.run(falseBlock).thenApply { future.complete(it) }
            } else {
                future.complete(false)
            }
        }
        return future
    }

    companion object {
        /**
         * 拓展 If 语句
         *
         * if* {...} {...} else {...}
         * if* {...} then {...} else {...}
         * if* {...} {symbol} {...} then {...}
         * if* {...} and/or {...} then {...}
         * */
        @VulKetherParser(
            id = "if-else",
            name = ["if*"],
            override = ["if"]
        )
        fun parse() = scriptParser { reader ->
            val expression = reader.parseCondition()
            val trueBlock = reader.tryNextBlock("then") ?: reader.nextBlock()
            val falseBlock = reader.tryNextBlock("else")

            ActionIfElse(expression, trueBlock, falseBlock)
        }

        /**
         * 解析条件语句块
         * @return 逆波兰表达式
         * */
        private fun QuestReader.parseCondition(): List<Any> {
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

        private fun QuestReader.nextCondition(): Condition {
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
    }

    private interface Condition {
        fun run(frame: ScriptFrame): CompletableFuture<Boolean>
    }

    private class SingleCondition(
        val condition: ParsedAction<*>
    ) : Condition {
        override fun run(frame: ScriptFrame): CompletableFuture<Boolean> {
            return frame.run(condition).thenApply { it.toBoolean(false) }
        }
    }

    private class LogicCondition(
        val left: ParsedAction<*>,
        val checkType: CheckType,
        val right: ParsedAction<*>,
    ) : Condition {
        override fun run(frame: ScriptFrame): CompletableFuture<Boolean> {
            return listOf(
                frame.run(left),
                frame.run(right)
            ).thenTake().thenApply {
                checkType.check(it[0], it[1])
            }
        }
    }

    private enum class LogicSymbol(val priority: Int) {

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
}