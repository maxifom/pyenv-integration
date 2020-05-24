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

        val re = Regex("\\d\\.\\d{1,2}\\.?\\d{0,2}", RegexOption.MULTILINE)
        val process = ProcessBuilder(state!!.pathToPyenv, "versions")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        process.waitFor()
        val responseText = process.inputStream.bufferedReader().readText()

        if (process.exitValue() != 0) {
            Notifications.Bus.notify(
                Notification(
                    "pyenv-integration",
                    "Pyenv installed versions listing failed",
                    responseText,
                    NotificationType.ERROR
                )
            )
        }

        var message = "Pyenv installed versions: \n"
        for (matchResult in re.findAll(responseText)) {
            message += matchResult.value + "\n"
        }

        Messages.showInfoMessage(e.project, message, "Pyenv installed versions")
    }
}