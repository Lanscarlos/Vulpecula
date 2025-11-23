package top.lanscarlos.vulpecula.module.bacikal.parser

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.reflect.hasAnnotation
import taboolib.library.kether.ParsedAction
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.common.applicative.ApplicativeRegistry
import top.lanscarlos.vulpecula.common.applicative.LocationApplicative
import top.lanscarlos.vulpecula.common.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.Expected
import top.lanscarlos.vulpecula.module.bacikal.annotation.Optional
import java.lang.reflect.Parameter
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2025/6/28
 */
class ClassActionParameter(
    val index: Int,
    val name: String,
    val isNullable: Boolean,
    val hasDefaultValue: Boolean,
    source: Parameter
) {

    /**
     * 参数类型
     * */
    val type: Class<*> = source.type

    /**
     * 参数前缀
     * */
    val prefix: List<String>

    /**
     * 参数修饰符
     * */
    val modifier: Modifier

    init {
        // 解析参数修饰符和前缀
        when {
            source.hasAnnotation(Expected::class.java) -> {
                modifier = Modifier.EXPECTED
                prefix = source.getAnnotation(Expected::class.java).values.toList()
            }
            source.hasAnnotation(Optional::class.java) -> {
                require(isNullable || hasDefaultValue) {
                    asLang("module-bacikal-exception-invalid-parameter-defined", Optional::class.java.simpleName, index, name)
                }
                modifier = Modifier.OPTIONAL
                prefix = source.getAnnotation(Optional::class.java).values.toList()
            }
            source.hasAnnotation(Additional::class.java) -> {
                require(isNullable || hasDefaultValue) {
                    asLang("module-bacikal-exception-invalid-parameter-defined", Additional::class.java.simpleName, index, name)
                }
                modifier = Modifier.ADDITIONAL
                prefix = source.getAnnotation(Additional::class.java).values.toList()
            }
            else -> {
                modifier = Modifier.NONE
                prefix = emptyList<String>()
            }
        }

        // 检查前缀
        if (modifier != Modifier.NONE) {
            require(prefix.isNotEmpty()) {
                asLang("module-bacikal-exception-empty-modifier-prefix", index, name)
            }
            require(prefix.all { it.toIntOrNull() == null }) {
                asLang("module-bacikal-exception-invalid-modifier-prefix", index, name)
            }
        }
    }

    fun read(reader: BacikalReader): BacikalAction<*> {
        when (type) {
            BacikalFrame::class.java -> return FrameAction
            Player::class.java -> return PlayerAction(isNullable)
            ProxyPlayer::class.java -> return ProxyPlayerAction(isNullable)
        }
        val action: ParsedAction<*> = when (modifier) {
            Modifier.NONE,
            Modifier.ADDITIONAL -> {
                // 附加参数前面前缀在本函数调用前已经验证过了
                reader.readAction()
            }
            Modifier.EXPECTED -> {
                reader.expectToken(prefix)
                reader.readAction()
            }
            Modifier.OPTIONAL -> {
                if (!reader.hasToken(prefix)) {
                    return DefaultAction(index, type)
                }
                reader.readAction()
            }
        }
        return when (type) {
            ParsedAction::class.java -> WrappedAction(action)
            org.bukkit.Location::class.java -> BukkitLocationAction(action, index, name, isNullable)
            Any::class.java -> GenericAction(action, index, name, isNullable)
            else -> ApplicativeAction(action, type, index, name, isNullable)
        }
    }

    /**
     * 语句参数修饰符
     * */
    enum class Modifier {
        NONE, EXPECTED, OPTIONAL, ADDITIONAL
    }

    object FrameAction : BacikalAction<BacikalFrame> {
        override fun execute(frame: BacikalFrame): CompletableFuture<BacikalFrame> {
            return CompletableFuture.completedFuture(frame)
        }
    }

    class PlayerAction(val isNullable: Boolean) : BacikalAction<Player> {
        override fun execute(frame: BacikalFrame): CompletableFuture<Player> {
            val player = frame.senderAsPlayer
            require(isNullable || player != null) {
                asLang("module-bacikal-exception-script-player-not-found")
            }
            return CompletableFuture.completedFuture(player)
        }
    }

    class ProxyPlayerAction(val isNullable: Boolean) : BacikalAction<ProxyPlayer> {
        override fun execute(frame: BacikalFrame): CompletableFuture<ProxyPlayer> {
            val player = frame.sender as? ProxyPlayer
            require(isNullable || player != null) {
                asLang("module-bacikal-exception-script-player-not-found")
            }
            return CompletableFuture.completedFuture(player)
        }
    }

    class WrappedAction(val action: ParsedAction<*>) : BacikalAction<ParsedAction<*>> {
        override fun execute(frame: BacikalFrame): CompletableFuture<ParsedAction<*>> {
            return CompletableFuture.completedFuture(action)
        }
    }

    class GenericAction(val source: ParsedAction<*>, val index: Int, val name: String, val isNullable: Boolean) : BacikalAction<Any> {

        override fun execute(frame: BacikalFrame): CompletableFuture<Any> {
            return frame.runAction(source).thenApply {
                if (it == null) {
                    require(isNullable) {
                        asLang("module-bacikal-exception-invalid-null-argument", index, name)
                    }
                    return@thenApply null
                }
                return@thenApply it
            }
        }

    }

    class BukkitLocationAction(val source: ParsedAction<*>, val index: Int, val name: String, val isNullable: Boolean) : BacikalAction<org.bukkit.Location> {
        override fun execute(frame: BacikalFrame): CompletableFuture<org.bukkit.Location> {
            return frame.runAction(source).thenApply {
                if (it == null) {
                    require(isNullable) {
                        asLang("module-bacikal-exception-invalid-null-argument", index, name)
                    }
                    return@thenApply null
                }
                return@thenApply LocationApplicative.convert(it).toBukkitLocation()
            }
        }
    }

    class ApplicativeAction<T: Any>(val source: ParsedAction<*>, type: Class<T>, val index: Int, val name: String, val isNullable: Boolean) : BacikalAction<T> {

        val applicative = ApplicativeRegistry.getApplicative(type)

        override fun execute(frame: BacikalFrame): CompletableFuture<T> {
            return frame.runAction(source).thenApply {
                if (it == null) {
                    require(isNullable) {
                        asLang("module-bacikal-exception-invalid-null-argument", index, name)
                    }
                    return@thenApply null
                }
                return@thenApply applicative.convert(it)
            }
        }
    }

}