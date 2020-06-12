package com.maxifom.pyenv_integration

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class ListAction : AnAction() {
    private var state: PluginState? = PluginSettings.getInstance().state

    override fun actionPerformed(e: AnActionEvent) {
        val process = ProcessBuilder(state!!.pathToPyenv, "versions")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        process.waitFor()

        if (process.exitValue() != 0) {
            val errorText = process.errorStream.bufferedReader().readText()
            Notifications.Bus.notify(
                Notification(
                    "pyenv-integration",
                    "Pyenv installed versions listing failed",
                    errorText,
                    NotificationType.ERROR
                )
            )
        }

        val versions = process.inputStream.bufferedReader().readLines()
        val sb = StringBuilder("Pyenv installed versions: \n")
        sb.append(versions.joinToString("\n"))

        Messages.showInfoMessage(e.project, sb.toString(), "Pyenv installed versions")
    }
}