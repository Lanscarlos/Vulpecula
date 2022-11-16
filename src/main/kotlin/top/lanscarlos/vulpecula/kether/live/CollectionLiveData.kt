package top.lanscarlos.vulpecula.kether.live

import taboolib.library.kether.ParsedAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.live
 *
 * @author Lanscarlos
 * @since 2022-11-17 00:19
 */
class CollectionLiveData(
    val value: Any
) : LiveData<Collection<*>> {

    override fun get(frame: ScriptFrame, def: Collection<*>): Collection<*> {
        return getOrNull(frame) ?: def
    }

    override fun getOrNull(frame: ScriptFrame): Collection<*>? {
        val it = if (value is ParsedAction<*>) {
            frame.run(value).join()
        } else value

        return when (it) {
            is Collection<*> -> it
            is Array<*> -> it.map { it }
            else -> if (it != null) setOf(it) else null
        }
    }
}