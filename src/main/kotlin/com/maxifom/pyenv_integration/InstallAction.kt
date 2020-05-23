package com.maxifom.pyenv_integration

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.ui.Messages
import com.intellij.util.io.readCharSequence
import java.io.BufferedReader
import java.util.concurrent.TimeUnit

class InstallAction : AnAction() {
    private var state: PluginState? = PluginSettings.getInstance().state


    override fun actionPerformed(e: AnActionEvent) {
        val versions = mutableMapOf<String, Set<String>>()
        try {
            val pb = ProcessBuilder(state!!.pathToPyenv, "install", "--list")
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            pb.waitFor(5, TimeUnit.SECONDS)
            val versionsText = pb.inputStream.bufferedReader().readLines()
            for (v in versionsText.listIterator(1)) {
                val vTrim = v.trim()
                if (vTrim.contains("-dev")) {
                    continue
                }
                val splitted = vTrim.split("-")
                var name = "python.org"
                var version: String
                if (splitted.count() > 1) {
                    name = splitted[0]
                    version = splitted.subList(1, splitted.size).joinToString("-")
                } else {
                    version = splitted[0]
                }


                versions.putIfAbsent(name, setOf(version))
                versions[name] = versions[name]!! + version
            }
        } catch (e: Exception) {
            print("Exception listing pyenv versions: $e")
        }

        println(versions)

        var name = Messages.showEditableChooseDialog(
            "Choose python branch to install",
            "Choose python branch",
            Messages.getQuestionIcon(),
            versions.keys.toTypedArray(),
            versions.keys.first(),
            null
        )

        println("""Chose name $name""")

        val version = Messages.showEditableChooseDialog(
            "Choose $name Python version to install",
            "Install $name Python",
            Messages.getQuestionIcon(),
            versions[name]!!.toTypedArray(),
            versions[name]!!.first(),
            null
        )

        println("""Chose version $version""")

        if (name == "python.org") {
            name = ""
        } else {
            name += "-"
        }

        println("Chose python $name$version")

        runBackgroundableTask("Pyenv Install $name$version", e.project, false) {
            val process = ProcessBuilder(state!!.pathToPyenv, "install123", "$name$version").start()
            process.waitFor()
            val exitValue = process.exitValue()

            if (exitValue != 0) {
                val sb = StringBuilder()
                for (line in process.errorStream.bufferedReader().lines()) {
                    sb.append(line)
                }
                println("Error installing python $name$version: $sb")

                Notifications.Bus.notify(
                    Notification(
                        "pyenv-integration",
                        "Python installation failed",
                        "$sb",
                        NotificationType.ERROR
                    )
                )

                return@runBackgroundableTask
            }


            Notifications.Bus.notify(
                Notification(
                    "pyenv-integration",
                    "Python installation finished",
                    "Python $name$version installed",
                    NotificationType.INFORMATION
                ),
                e.project
            )
        }
    }
}