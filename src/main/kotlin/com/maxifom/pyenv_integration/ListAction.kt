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

        val sb = StringBuilder("Pyenv installed versions: \n")
        for (line in responseText.lineSequence()) {
            if (line.contains("*")) {
                continue
            }
            val vTrim = line.trim()
            if (vTrim.contains("-dev")) {
                continue
            }
            val splitted = vTrim.split("-")
            var name = ""
            var version: String
            if (splitted.count() > 1) {
                name = splitted[0]
                version = splitted.subList(1, splitted.size).joinToString("-")
            } else {
                version = splitted[0]
            }

            if (name != "") {
                name = "$name-"
            }

            sb.appendln("$name$version")
        }

        Messages.showInfoMessage(e.project, sb.toString(), "Pyenv installed versions")
    }
}