package top.lanscarlos.vulpecula.bacikal

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2024-05-14 02:29
 */
object BacikalTest {

    @Awake(LifeCycle.ENABLE)
    fun onTest() {
        info("BacikalTest onTest...")
        BacikalRegistry.registerAction(File(getDataFolder(), "item-3.0.0.jar"))
        info("BacikalTest onTest... x2")
    }

}