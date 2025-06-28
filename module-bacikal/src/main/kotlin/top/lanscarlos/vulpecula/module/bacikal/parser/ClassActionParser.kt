package top.lanscarlos.vulpecula.module.bacikal.parser

import taboolib.library.kether.*
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.chat.StandardColors
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.action.ActionSource
import top.lanscarlos.vulpecula.module.bacikal.exception.ClassActionRegisterException
import java.util.LinkedList
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * 类式语句解析器
 *
 * @author Lanscarlos
 * @since 2024-11-20 11:11
 */
class ClassActionParser(
    id: String,
    name: String,
    aliases: Array<String>,
    namespace: String,
    description: String,
    metadata: Array<String>,
    override val source: ActionSource,
    resolver: ClassActionResolver
) : AbstractActionParser(id, name, aliases, namespace, description) {

    private val function: ClassActionFunction

    private val parameters: List<ClassActionParameter> get() = function.parameters

    init {
        try {
            function = ClassActionFunction(metadata, resolver)
        } catch (cause: Exception) {
            throw ClassActionRegisterException(id, cause)
        }
    }

    override fun buildStructure(depth: Int): ComponentText {
        return onDrawStructure(depth, 0).first()
    }

    override fun onDrawStructure(maxDepth: Int, currentDepth: Int): List<ComponentText> {
        // 绘制语句头
        val name = if (currentDepth == 0) id.replace('.', '-') else name
        val component = Components.text(name).color(StandardColors.RED).resetColor()
        // TODO 增加悬浮参数

        // 绘制参数
        for ((index, parameter) in parameters.withIndex()) {
            if (maxDepth >= 0 && index + currentDepth >= maxDepth) {
                component.append(" ...")
                break
            }
            if (parameter.type == BacikalFrame::class.java) {
                continue
            }
            component.append(" ")
            when (parameter.modifier) {
                ClassActionParameter.Modifier.NONE -> {
                    component += Components
                        .text("<${parameter.name}: ${parameter.type.simpleName}>")
                        .color(StandardColors.WHITE)
                }
                ClassActionParameter.Modifier.EXPECTED -> {
                    component += Components
                        .text(parameter.prefix.first())
                        .color(StandardColors.GRAY)
                        .hoverText(parameter.prefix.toList().toString())
                    component.append(" ")
                    component += Components
                        .text("<${parameter.name}: ${parameter.type.simpleName}>")
                        .color(StandardColors.GRAY)
                }
                ClassActionParameter.Modifier.OPTIONAL -> {
                    component += Components
                        .text("[")
                        .color(StandardColors.DARK_GRAY)
                    component += Components
                        .text(parameter.prefix.first())
                        .color(StandardColors.DARK_GRAY)
                        .hoverText(parameter.prefix.toList().toString())
                    component.append(" ")
                    component += Components
                        .text("<${parameter.name}: ${parameter.type.simpleName}>")
                        .color(StandardColors.DARK_GRAY)
                    component += Components
                        .text("]")
                        .color(StandardColors.DARK_GRAY)
                }
                ClassActionParameter.Modifier.ADDITIONAL -> {
                    component += Components
                        .text("--")
                        .color(StandardColors.DARK_PURPLE)
                    component += Components
                        .text(parameter.prefix.first())
                        .color(StandardColors.DARK_PURPLE)
                        .hoverText(parameter.prefix.toList().toString())
                    component.append(" ")
                    component += Components
                        .text("<${parameter.name}: ${parameter.type.simpleName}>")
                        .color(StandardColors.DARK_PURPLE)
                }
            }
            component.resetColor()
        }
        return listOf(component)
    }

    /**
     * 解析语句
     * */
    override fun <T : Any?> resolve(source: QuestReader): QuestAction<T> {
        // 编译为可执行的 QuestAction
        val actions: Array<BacikalAction<*>> = Array(parameters.size) { UninitializedAction }
        val reader = DefaultReader(source)
        val additional = mutableMapOf<String, ClassActionParameter>()
        var breakIndex = 0 // 断点索引

        // 解析参数并提取附加参数
        for ((index, parameter) in parameters.withIndex()) {
            when {
                parameter.modifier == ClassActionParameter.Modifier.ADDITIONAL -> {
                    // 提取附加参数
                    for (prefix in parameter.prefix) {
                        additional[prefix] = parameter
                    }
                }
                additional.isEmpty() -> {
                    // 非附加参数, 并且当前未检测到附加参数，正常解析参数
                    actions[index] = parameter.read(reader)
                }
                else -> {
                    // 已检索到附加参数，当前为第一个非附加参数，设置断点
                    breakIndex = index
                    break
                }
            }
        }

        // 解析附加参数
        if (additional.isNotEmpty()) {
            val regex = "--\\D+".toRegex()
            while (reader.peekToken().matches(regex)) {
                val prefix = reader.readToken().substring(1)
                val parameter = additional[prefix]
                    ?: error(asLang("module-bacikal-exception-unknown-additional-parameter", prefix))
                actions[parameter.index] = parameter.read(reader)
            }

            // 将未初始化的参数替换为缺省值
            for (i in actions.indices) {
                if (actions[i] is UninitializedAction) {
                    actions[i] = DefaultAction(i, parameters[i].type)
                }
            }

            // 读取剩余非附加参数
            if (breakIndex > 0) {
                // 已定位断点，跳过断点前的语句;
                // 断点必然大于零，因为前面至少有一个附加参数
                for (index in breakIndex until parameters.size) {
                    actions[index] = parameters[index].read(reader)
                }
            }
        }

        return QuestActionResolver(actions)
    }

    inner class QuestActionResolver<T>(val actions: Array<BacikalAction<*>>) : QuestAction<T>() {

        // 计算缺省掩码值
        val mask: Int = actions.fold(0) { acc, action ->
            if (action !is DefaultAction) {
                return@fold acc
            }
            acc or action.mask
        }

        @Suppress("UNCHECKED_CAST")
        override fun process(source: QuestContext.Frame): CompletableFuture<T> {
            val frame = DefaultFrame(source)
            val future = process(actions, frame)
            return if (function.isUseFutureReturn) {
                future.thenCompose {
                    function.invoke(mask, it.toTypedArray()) as CompletableFuture<T>
                }
            } else {
                future.thenApply {
                    function.invoke(mask, it.toTypedArray()) as T
                }
            }
        }

    }

    private fun process(queue: Array<BacikalAction<*>>, frame: BacikalFrame): CompletableFuture<out List<Any?>> {
        if (queue.isEmpty()) {
            return CompletableFuture.completedFuture(emptyList())
        }

        if (queue.size == 1) {
            val action = queue[0]
            val future = action.execute(frame)
            return future.handle { result, ex ->
                if (ex != null) {
                    // 中断队列
                    throw ex
                }
                listOf(result)
            }
        }

        // 构建顺序执行链
        return queue.fold(CompletableFuture.completedFuture<LinkedList<Any?>>(LinkedList())) { acc, action ->
            acc.thenCompose { results ->
                action.execute(frame).handle { result, ex ->
                    if (ex != null) {
                        // 中断队列
                        throw ex
                    }
                    results.add(result)
                    results
                }
            }
        }
    }

}