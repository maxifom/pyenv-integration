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

        val versionsText = listProcess.inputStream.bufferedReader().readLines().drop(1)
            .map { it.trim() }

        val version = Messages.showEditableChooseDialog(
            "Choose python version to install",
            "Choose python version",
            Messages.getQuestionIcon(),
            versionsText.toTypedArray(),
            versionsText.first(),
            null
        )

        if (version == null) {
            return
        }

        runBackgroundableTask("Pyenv Install $version", e.project, false) {
            val process: Process
            try {
                process = ProcessBuilder(state!!.pathToPyenv, "install", version).start()
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
                    "Python $version installed",
                    NotificationType.INFORMATION
                ),
                e.project
            )
        }
    }
}