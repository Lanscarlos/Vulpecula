package top.lanscarlos.vulpecula.bacikal.parser

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.ScriptFrame
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2023-08-27 22:17
 */
class DefaultFrame(val source: ScriptFrame) : BacikalFrame {

    override var sender: ProxyCommandSender?
        get() = (source.context() as? ScriptContext)?.sender
        set(value) {
            (source.context() as? ScriptContext)?.sender = value
        }

    override var senderAsPlayer: ProxyPlayer?
        get() = sender as? ProxyPlayer
        set(value) {
            sender = value
        }

    override fun get(key: String): Any? {
        return getVariable(key)
    }

    override fun set(key: String, value: Any?): Any? {
        return setVariable(key, value)
    }

    override fun <T> getVariable(key: String): T? {
        return source.variables().get<T>(key).orElse(null)
    }

    override fun <T> getVariable(key: String, default: T): T {
        return source.variables().get<T>(key).orElse(default)
    }

    override fun <T> getVariables(vararg key: String): T? {
        return key.firstNotNullOfOrNull { getVariable<T>(it) }
    }

    override fun setVariable(key: String, value: Any?): Any? {
        val previousValue = getVariable<Any?>(key)
        source.variables().set(key, value)
        return previousValue
    }

    override fun setVariables(vararg key: String, value: Any?) {
        key.forEach { setVariable(it, value) }
    }

    override fun newFrame(name: String): BacikalFrame {
        return DefaultFrame(source.newFrame(name))
    }

    override fun newFrame(action: ParsedAction<*>): BacikalFrame {
        return DefaultFrame(source.newFrame(action))
    }

    override fun <T> runAction(action: ParsedAction<T>, newFrame: Boolean): CompletableFuture<T> {
        return if (newFrame) {
            source.newFrame(action).run()
        } else {
            action.process(source)
        }
    }

}