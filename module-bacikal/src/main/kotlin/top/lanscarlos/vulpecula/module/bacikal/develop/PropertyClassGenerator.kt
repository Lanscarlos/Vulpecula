package top.lanscarlos.vulpecula.module.bacikal.develop

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.common5.util.replace
import taboolib.library.reflex.AnalyseMode
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.common.applicative.ApplicativeRegistry
import top.lanscarlos.vulpecula.common.applicative.Applicatives
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.develop
 *
 * @author Lanscarlos
 * @since 2025/8/31
 */
object PropertyClassGenerator {

    data class Getter(val name: String)

    data class Setter(val name: String, val parameterType: Class<*>, val isNullable: Boolean)

    @Awake(LifeCycle.ACTIVE)
    fun onActive() {
        info("尝试生成 Entity 包下所有属性")
        try {
            for (clazz in PackageScanner.getClassesInPackage(Entity::class.java.`package`.name)) {
                generate(clazz, "entity")
            }
            info("Entity 属性包生成完毕...")
        } catch (e: Exception) {
            e.printStackTrace()
            warning("Entity 属性包生成失败...")
        }
    }

    fun generate(target: Class<*>, module: String) {
        val reflexClass = ReflexClass.of(target, AnalyseMode.ASM_ONLY)

        // 读取所有标准 bean 方法名称
        val getters = mutableListOf<Getter>()
        val setters = mutableListOf<Setter>()
        for (method in reflexClass.structure.methods) {
            when {
                method.name.startsWith("get") && method.parameter.isEmpty() -> {
                    // getter 方法
                    getters += Getter(method.name)
                }
                method.name.startsWith("set") && method.parameter.size == 1 -> {
                    // setter 方法
                    val parameterType = method.parameterTypes.single()
                    val isNullable = method.parameter.single().isAnnotationPresent(org.jetbrains.annotations.Nullable::class.java)
                    setters += Setter(method.name, parameterType, isNullable)
                }
            }
        }

        val getter = generateReadMethod(getters)
        val setter = generateWriteMethod(setters)
        val template = this.javaClass.classLoader.getResource("template/property.kt")!!.readText()
            .replace(
                "\${module}" to module,
                "\${import}" to target.name,
                "\${time}" to SimpleDateFormat("yyyy/MM/dd").format(Date()),
                "\${name}" to "${target.simpleName}Property",
                "\${target}" to target.simpleName,
                "\${getters}" to getter,
                "\${setters}" to setter,
            )
        val output = File(getDataFolder(), "develop/${target.simpleName}Property.kt")
        if (output.parentFile.exists().not()) {
            output.parentFile.mkdirs()
        }
        output.writeText(template)
    }

    fun generateReadMethod(getters: List<Getter>): String {
        val builder = StringBuilder()
        for ((i, getter) in getters.withIndex()) {
            val name = getter.name.substring(3).replaceFirstChar { if (it.isUpperCase()) it.lowercaseChar() else it }
            if (i > 0) {
                builder.append("                ") // 缩进
            }
            builder.append('"').append(name).append('"')
                .append(" -> ")
                .append("instance.${getter.name}()")
            if (i != getters.lastIndex) {
                builder.append('\n')
            }
        }
        return builder.toString()
    }

    fun  generateWriteMethod(setters: List<Setter>): String {
        val builder = StringBuilder()
        for ((i, setter) in setters.withIndex()) {
            val name = setter.name.substring(3).replaceFirstChar { if (it.isUpperCase()) it.lowercaseChar() else it }
            val applicative = try {
                ApplicativeRegistry.getApplicative(setter.parameterType)
            } catch (e: Exception) {
                warning(e.localizedMessage)
                continue
            }
            if (i > 0) {
                builder.append("                ") // 缩进
            }
            builder.append('"').append(name).append('"')
                .append(" -> ")
                .append("instance.${setter.name}(")
            if (setter.isNullable) {
                builder.append("value?.let(${applicative.javaClass.simpleName}::convert)")
            } else {
                builder.append("value.let(${applicative.javaClass.simpleName}::convert)")
            }
            builder.append(")")
            if (i != setters.lastIndex) {
                builder.append('\n')
            }
        }
        return builder.toString()
    }

}