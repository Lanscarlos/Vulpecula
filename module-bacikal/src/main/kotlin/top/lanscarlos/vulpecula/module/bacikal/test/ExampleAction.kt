package top.lanscarlos.vulpecula.module.bacikal.test

import taboolib.common.platform.function.info
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.annotation.Optional
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.test
 *
 * @author Lanscarlos
 * @since 2025/6/18
 */
@BacikalParser("example.develop")
object ActionExampleDevelop : ClassActionResolver {
    fun resolve(
    ) = info("example.develop")
}

@BacikalParser("example.action.registry")
object ActionExampleActionRegistry : ClassActionResolver {
    fun resolve(
    ) = info("example.action.registry")
}

@BacikalParser("example.action.timing")
object ActionExampleActionTiming : ClassActionResolver {
    fun resolve(
    ) = info("example.action.timing")
}

@BacikalParser("example.script.run")
object ActionExampleScriptRun : ClassActionResolver {
    fun resolve(
        id: String,
        @Optional(["sender"]) sender: String,
        @Optional(["args"]) args: String
    ) = info("example.script.run")
}

@BacikalParser("example.script.run-silent")
object ActionExampleScriptRunSilent : ClassActionResolver {
    fun resolve(
        id: String,
        @Optional(["sender"]) sender: String,
        @Optional(["args"]) args: String
    ) = info("example.script.run-silent")
}

@BacikalParser("example.script.stop")
object ActionExampleScriptStop : ClassActionResolver {
    fun resolve(
        id: String
    ) = info("example.script.stop")
}

@BacikalParser("example.script.task.stop")
object ActionExampleScriptTaskStop : ClassActionResolver {
    fun resolve(
        id: String
    ) = info("example.script.task.stop")
}

@BacikalParser("example.script.task.list")
object ActionExampleScriptTaskList : ClassActionResolver {
    fun resolve(
    ) = info("example.script.task.list")
}

@BacikalParser("example.script.reload")
object ActionExampleScriptReload : ClassActionResolver {
    fun resolve(
    ) = info("example.script.reload")
}

@BacikalParser("example.schedule.start")
object ActionExampleScheduleStart : ClassActionResolver {
    fun resolve(
        id: String,
        @Optional(["pid"]) pid: String,
        @Additional(["sender"]) sender: String,
        @Additional(["args"]) args: String
    ) = info("example.schedule.start")
}

@BacikalParser("example.schedule.pause")
object ActionExampleSchedulePause : ClassActionResolver {
    fun resolve(
        id: String,
        @Optional(["pid"]) pid: String
    ) = info("example.schedule.pause")
}

@BacikalParser("example.schedule.resume")
object ActionExampleScheduleResume : ClassActionResolver {
    fun resolve(
        id: String,
        @Optional(["pid"]) pid: String
    ) = info("example.schedule.resume")
}

@BacikalParser("example.schedule.stop")
object ActionExampleScheduleStop : ClassActionResolver {
    fun resolve(
        id: String,
        @Optional(["pid"]) pid: String
    ) = info("example.schedule.stop")
}

@BacikalParser("example.schedule.detail")
object ActionExampleScheduleDetail : ClassActionResolver {
    fun resolve(
        @Optional(["pid"]) pid: String
    ) = info("example.schedule.detail")
}

@BacikalParser("example.schedule.reload")
object ActionExampleScheduleReload : ClassActionResolver {
    fun resolve(
    ) = info("example.schedule.reload")
}

@BacikalParser("example.command.reload")
object ActionExampleCommandReload : ClassActionResolver {
    fun resolve(
    ) = info("example.command.reload")
}

@BacikalParser("example.reload")
object ActionExampleReload : ClassActionResolver {
    fun resolve(
        @Optional(["service"]) service: String
    ) = info("example.reload")
}

@BacikalParser("example.dispatcher.reload")
object ActionExampleDispatcherReload : ClassActionResolver {
    fun resolve(
    ) = info("example.dispatcher.reload")
}

@BacikalParser("example.dispatcher.enable")
object ActionExampleDispatcherEnable : ClassActionResolver {
    fun resolve(
        id: String
    ) = info("example.dispatcher.enable")
}

@BacikalParser("example.dispatcher.disable")
object ActionExampleDispatcherDisable : ClassActionResolver {
    fun resolve(
        id: String
    ) = info("example.dispatcher.disable")
}

@BacikalParser("example.eval")
object ActionExampleEval : ClassActionResolver {
    fun resolve(
        content: String
    ) = info("example.eval")
}