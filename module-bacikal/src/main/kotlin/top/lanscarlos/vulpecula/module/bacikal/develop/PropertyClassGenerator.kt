package top.lanscarlos.vulpecula.module.bacikal.develop

import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashSet

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

    val warnings = HashSet<String>()

//    @Awake(LifeCycle.ACTIVE)
    fun onActive() {
        File(getDataFolder(), "develop").deleteRecursively()
        info("尝试生成属性包")
        try {
            val packageName = Entity::class.java.`package`.name
            generate(Entity::class.java, packageName, "entity")
            generate(LivingEntity::class.java, packageName, "entity")
            generate(Player::class.java, packageName, "entity")
            generate(Damageable::class.java, packageName, "entity")
            for (info in warnings) {
                warning(info)
            }
            info("属性包生成完毕...")
        } catch (e: Exception) {
            e.printStackTrace()
            warning("属性包生成失败...")
        }
    }

    fun generate(target: Class<*>, packageName: String, module: String) {
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

        if (getters.isEmpty() && setters.isEmpty()) {
            // 无可用属性
            warning("类 ${target.name} 无可用字段属性")
            return
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
        val name = target.name.substringAfter("$packageName.").replace('.', '/')
        val output = File(getDataFolder(), "develop/${name}Property.kt")
        if (output.parentFile.exists().not()) {
            output.parentFile.mkdirs()
        }
        output.writeText(template)
    }

    fun generateReadMethod(getters: List<Getter>): String {
        val builder = StringBuilder()
        for ((i, getter) in getters.withIndex()) {
            val name = getter.name.substring(3).replaceFirstChar { if (it.isUpperCase()) it.lowercaseChar() else it }
            if (builder.isNotBlank()) {
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
                warnings.add(e.localizedMessage)
//                warning(e.localizedMessage)
                continue
            }
            if (builder.isNotBlank()) {
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