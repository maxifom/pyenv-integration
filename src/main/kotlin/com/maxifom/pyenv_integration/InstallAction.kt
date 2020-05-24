package com.maxifom.pyenv_integration

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.ui.Messages
import java.lang.Exception

class InstallAction : AnAction() {
    private var state: PluginState? = PluginSettings.getInstance().state

    override fun actionPerformed(e: AnActionEvent) {
        val versions = mutableMapOf<String, Set<String>>()

        val listProcess: Process
        try {
            listProcess = ProcessBuilder(state!!.pathToPyenv, "install", "--list").start()
            listProcess.waitFor()
        } catch (e: Exception) {
            Notifications.Bus.notify(
                Notification(
                    "pyenv-integration",
                    "Pyenv version listing failed",
                    e.toString(),
                    NotificationType.ERROR
                )
            )

            return
        }

        if (listProcess.exitValue() != 0) {
            val errorText = listProcess.errorStream.bufferedReader().readText()

            Notifications.Bus.notify(
                Notification(
                    "pyenv-integration",
                    "Pyenv version listing failed",
                    errorText,
                    NotificationType.ERROR
                )
            )
            return
        }

        val versionsText = listProcess.inputStream.bufferedReader().readLines()
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

        var name = Messages.showEditableChooseDialog(
            "Choose python branch to install",
            "Choose python branch",
            Messages.getQuestionIcon(),
            versions.keys.toTypedArray(),
            versions.keys.first(),
            null
        )

        if (name == null) {
            return
        }

        val version = Messages.showEditableChooseDialog(
            "Choose $name Python version to install",
            "Install $name Python",
            Messages.getQuestionIcon(),
            versions[name]!!.toTypedArray(),
            versions[name]!!.first(),
            null
        )

        if (version == null) {
            return
        }

        if (name == "python.org") {
            name = ""
        } else {
            name += "-"
        }

        runBackgroundableTask("Pyenv Install $name$version", e.project, false) {
            val process: Process
            try {
                process = ProcessBuilder(state!!.pathToPyenv, "install", "$name$version").start()
                process.waitFor()
            } catch (e: Exception) {
                Notifications.Bus.notify(
                    Notification(
                        "pyenv-integration",
                        "Python installation failed",
                        e.toString(),
                        NotificationType.ERROR
                    )
                )

                return@runBackgroundableTask
            }


            if (process.exitValue() != 0) {
                val errorText = process.errorStream.bufferedReader().readText()

                Notifications.Bus.notify(
                    Notification(
                        "pyenv-integration",
                        "Python installation failed",
                        errorText,
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