package com.maxifom.pyenv_integration

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.ui.Messages
import java.lang.Exception

class UninstallAction : AnAction() {
    private var state: PluginState? = PluginSettings.getInstance().state

    override fun actionPerformed(e: AnActionEvent) {
        val listProcess = ProcessBuilder(state!!.pathToPyenv, "versions")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        listProcess.waitFor()

        if (listProcess.exitValue() != 0) {
            val errorText = listProcess.inputStream.bufferedReader().readText()
            Notifications.Bus.notify(
                Notification(
                    "pyenv-integration",
                    "Pyenv installed versions listing failed",
                    errorText,
                    NotificationType.ERROR
                )
            )
        }

        val versionText = listProcess.inputStream.bufferedReader()
            .readLines().filter { !it.contains("system") }.map { it.trim() }

        val version = Messages.showEditableChooseDialog(
            "Choose python version to uninstall",
            "Choose python version to uninstall",
            Messages.getQuestionIcon(),
            versionText.toTypedArray(),
            versionText.first(),
            null
        )

        if (version == null) {
            return
        }

        runBackgroundableTask("Pyenv uninstall $version", e.project, false) {
            val process: Process
            try {
                process = ProcessBuilder(state!!.pathToPyenv, "uninstall", "-f", version).start()
                process.waitFor()
            } catch (e: Exception) {
                Notifications.Bus.notify(
                    Notification(
                        "pyenv-integration",
                        "Python uninstall failed",
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
                        "Python uninstall failed",
                        errorText,
                        NotificationType.ERROR
                    )
                )

                return@runBackgroundableTask
            }


            Notifications.Bus.notify(
                Notification(
                    "pyenv-integration",
                    "Python uninstall finished",
                    "Python $version installed",
                    NotificationType.INFORMATION
                ),
                e.project
            )
        }
    }
}