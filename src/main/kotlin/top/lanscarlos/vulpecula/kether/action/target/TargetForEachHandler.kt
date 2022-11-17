package top.lanscarlos.vulpecula.kether.action.target

import taboolib.library.kether.QuestReader
import taboolib.module.kether.run
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.target
 *
 * @author Lanscarlos
 * @since 2022-11-17 00:30
 */
object TargetForEachHandler : ActionTarget.Reader {

    override val name: Array<String> = arrayOf("foreach")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionTarget.Handler {
        val source = if (isRoot) reader.readCollection() else null
        val token = if (reader.hasNextToken("by")) reader.nextToken() else "it"
        val loopBody = reader.nextBlock()

        return acceptHandler(source) { collection ->
            // 临时存储原有变量
            val origin = this.getVariable<Any?>(token)

            for (it in collection) {
                // 覆盖变量
                this.setVariable(token, it)
                this.run(loopBody).join()
            }

            // 恢复原有变量
            this.setVariable(token, origin)
            collection
        }
    }
}