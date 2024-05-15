package top.lanscarlos.vulpecula

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.lang.asLangText
import top.lanscarlos.vulpecula.utils.timing
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula
 *
 * @author Lanscarlos
 * @since 2023-08-14 17:07
 */
object Vulpecula {

    @Config
    lateinit var config: Configuration
        private set

    val reloadable = linkedMapOf<String, Supplier<String?>>()

    init {
        registerReloadable("config") {
            try {
                val start = timing()
                config.reload()
                console().asLangText("Config-Load-Succeeded", timing(start))
            } catch (ex: Exception) {
                console().asLangText("Config-Load-Failed", ex.localizedMessage ?: "null")
            }
        }
    }

    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        reload()
    }

    /**
     * 重载模块
     *
     * @param module 模块名称 为空则重载所有模块
     * @return 重载信息
     * */
    fun reload(vararg module: String): List<String> {
        return if (module.isEmpty()) {
            // 重载所有模块
            reloadable.values.mapNotNull { it.get() }
        } else {
            // 重载指定模块
            module.mapNotNull { reloadable[it]?.get() }
        }
    }

    /**
     * 注册可重载模块
     * */
    fun registerReloadable(name: String, reloadable: Supplier<String?>) {
        this.reloadable[name] = reloadable
    }
}