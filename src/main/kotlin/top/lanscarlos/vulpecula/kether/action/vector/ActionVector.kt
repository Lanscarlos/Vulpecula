package top.lanscarlos.vulpecula.kether.action.vector

import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.internal.ClassInjector
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.VectorLiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.nextPeek
import top.lanscarlos.vulpecula.utils.readDouble
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.vector
 *
 * @author Lanscarlos
 * @since 2022-11-11 13:33
 */
class ActionVector : ScriptAction<Any?>() {

    private val handlers = mutableListOf<Handler>()

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        var previous: Vector? = null
        for (handler in handlers) {
            if (handler is Transfer) {
                previous = handler.handle(frame, previous)
            } else {
                return CompletableFuture.completedFuture(
                    handler.handle(frame, previous)
                )
            }
        }
        return CompletableFuture.completedFuture(previous)
    }

    companion object : ClassInjector(packageName = ActionVector::class.java.packageName) {

        private val registry = mutableMapOf<String, Reader>()

        fun registerReader(reader: Reader) {
            reader.name.forEach { registry[it] = reader }
        }

        override fun visitStart(clazz: Class<*>, supplier: Supplier<*>?) {
            if (!Reader::class.java.isAssignableFrom(clazz)) return

            val reader = let {
                if (supplier?.get() != null) {
                    supplier.get()
                } else try {
                    clazz.getDeclaredConstructor().newInstance()
                } catch (e: Exception) {
                    null
                }
            } as? Reader ?: return

            registerReader(reader)
        }

        @VulKetherParser(
            id = "vector",
            name = ["vec", "vector"]
        )
        fun parser() = scriptParser { reader ->
            val action = ActionVector()
            do {
                reader.mark()
                val it = reader.nextToken()
                val isRoot = action.handlers.isEmpty()

                action.handlers += registry[it]?.read(reader, it, isRoot) ?: let { _ ->
                    reader.reset()
                    VectorBuildHandler.read(reader, it, isRoot)
                }

                // 判断管道是否已关闭
                if (action.handlers.lastOrNull() !is Transfer) {
                    if (reader.hasNextToken(">>")) {
                        error("Cannot use \">> ${reader.nextPeek()}\", previous action \"$it\" has closed the pipeline.")
                    }
                    break
                }
            } while (reader.hasNextToken(">>"))

            return@scriptParser action
        }
    }

    /**
     * 处理后返回任意对象
     * */
    interface Handler {
        fun handle(frame: ScriptFrame, previous: Vector?): Any?
    }

    /**
     * 处理后返回 Vector 对象，供下一处理器使用
     * */
    interface Transfer : Handler {
        override fun handle(frame: ScriptFrame, previous: Vector?): Vector
    }

    /**
     * 读取语句
     * */
    interface Reader {

        val name: Array<String>

        /**
         * @param input 传入的 name
         * @param isRoot 是否为队列最前端
         * @return 处理器
         * */
        fun read(reader: QuestReader, input: String, isRoot: Boolean): Handler

        /**
         * 是否复制 Vector
         * */
        fun QuestReader.isReproduced(): Boolean {
            return !this.hasNextToken("not-reproduced", "not-rep", "not-clone", "-n")
        }

        /**
         * 读取期望 Vector 数据
         * 主要用于运算
         *
         * @param expect 期望前缀，若找不到则使用 other 来构建 Vector
         * @param other 用来标识构建 Vector 的前缀
         * */
        fun QuestReader.expectVector(expect: String, other: String): LiveData<Vector> {
            return if (this.hasNextToken(expect)) {
                VectorLiveData(this.nextBlock())
            } else {
                this.expect(other)
                val x = this.readDouble()
                val y = this.readDouble()
                val z = this.readDouble()
                VectorLiveData(Triple(x, y, z))
            }
        }

        /**
         * 返回任意对象
         * */
        fun handle(func: ScriptFrame.(vector: Vector?) -> Any?): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: Vector?): Any? {
                    return func(frame, previous)
                }
            }
        }

        /**
         * 接收 Vector 返回任意对象
         * */
        fun acceptHandler(source: LiveData<Vector>?, func: ScriptFrame.(vector: Vector) -> Any?): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: Vector?): Any? {
                    val vec = previous ?: source?.getOrNull(frame) ?: error("No vector select.")
                    return func(frame, vec)
                }
            }
        }

        /**
         * 返回 Vector 对象
         * */
        fun transfer(func: ScriptFrame.(vector: Vector?) -> Vector): Handler {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: Vector?): Vector {
                    return func(frame, previous)
                }
            }
        }

        /**
         * 接收 Vector 并返回 Vector 对象
         * */
        fun acceptTransfer(source: LiveData<Vector>?, reproduced: Boolean, func: ScriptFrame.(vector: Vector) -> Vector): Transfer {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: Vector?): Vector {
                    val vec = previous ?: source?.getOrNull(frame) ?: error("No vector select.")
                    return func(frame, vec.let { if (reproduced) vec.clone() else vec })
                }
            }
        }
    }
}