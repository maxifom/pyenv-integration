package com.maxifom.pyenv_integration

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import java.io.File
import java.util.concurrent.TimeUnit

class ListAction : AnAction() {
    private var state: PluginState? = PluginSettings.getInstance().state

    override fun actionPerformed(e: AnActionEvent) {

        val re = Regex("\\d\\.\\d{1,2}\\.?\\d{0,2}", RegexOption.MULTILINE)
        var message = "No versions found"
        try {
            val pb = ProcessBuilder(state!!.pathToPyenv, "versions")
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            pb.waitFor(5, TimeUnit.SECONDS)
            val versionsText = pb.inputStream.bufferedReader().readText()
            message = "Versions: \n"
            for (matchResult in re.findAll(versionsText)) {
                message += matchResult.value + "\n"
            }
        } catch (e: Exception) {
            print(e)
        }

        Messages.showMessageDialog(
            e.project,
            message,
            "Installed versions",
            Messages.getInformationIcon()
        )
    }
}