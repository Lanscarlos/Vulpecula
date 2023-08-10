package top.lanscarlos.vulpecula.bacikal.action.illusion

import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestContext
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptActionParser
import taboolib.module.kether.ScriptFrame
import taboolib.module.nms.sendPacket
import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.BacikalReader
import top.lanscarlos.vulpecula.bacikal.LiveData
import top.lanscarlos.vulpecula.internal.ClassInjector
import top.lanscarlos.vulpecula.utils.getVariable
import top.lanscarlos.vulpecula.utils.playerOrNull
import top.lanscarlos.vulpecula.utils.toBukkit
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.illusion
 *
 * @author Lanscarlos
 * @since 2023-08-09 23:00
 */
class ActionIllusion : QuestAction<Any?>() {

    lateinit var handler: Bacikal.Parser<Any?>

    fun resolve(reader: QuestReader): QuestAction<Any?> {
        val next = reader.nextToken()
        handler = registry[next.lowercase()]?.resolve(Reader(next, reader))
            ?: error("Unknown sub action \"$next\" at illusion action.")

        return this
    }

    override fun process(frame: ScriptFrame): CompletableFuture<Any?> {
        return handler.action.run(frame)
    }

    /*
    * 自动注册包下所有解析器 Resolver
    * */
    @Awake(LifeCycle.LOAD)
    companion object : ClassInjector() {

        private val registry = mutableMapOf<String, Resolver>()

        /**
         * 向 Inventory 语句注册子语句
         * @param resolver 子语句解析器
         * */
        fun registerResolver(resolver: Resolver) {
            resolver.name.forEach { registry[it.lowercase()] = resolver }
        }

        override fun visitStart(clazz: Class<*>, supplier: Supplier<*>?) {
            if (!Resolver::class.java.isAssignableFrom(clazz)) return

            val resolver = let {
                if (supplier?.get() != null) {
                    supplier.get()
                } else try {
                    clazz.getDeclaredConstructor().newInstance()
                } catch (e: Exception) {
                    null
                }
            } as? Resolver ?: return

            registerResolver(resolver)
        }

        @BacikalParser(
            id = "illusion",
            aliases = ["illusion"]
        )
        fun parser() = ScriptActionParser<Any?> {
            ActionIllusion().resolve(this)
        }
    }

    /**
     * 语句解析器
     * */
    interface Resolver {

        val name: Array<String>

        fun resolve(reader: Reader): Bacikal.Parser<Any?>
    }

    /**
     * 语句读取器
     * */
    class Reader(val token: String, source: QuestReader) : BacikalReader(source) {
        fun source(): LiveData<List<Player>> {
            return LiveData {
                Bacikal.Action { frame ->
                    val hallucinators = frame.getVariable<List<Player>>("@Hallucinators") ?: listOf(
                        frame.playerOrNull()?.toBukkit() ?: error("No player selected. [ERROR: illusion@$token]")
                    )
                    CompletableFuture.completedFuture(hallucinators)
                }
            }
        }
    }
}