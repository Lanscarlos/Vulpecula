package top.lanscarlos.vulpecula.bacikal

import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuest
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-09-03 21:35
 */
interface BacikalScript : BacikalQuest {

    /**
     * 脚本文件
     * */
    val file: File

}