package com.maxifom.pyenv_integration

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.Messages
import com.intellij.ui.layout.chooseFile

class ChoosePyEnvPathAction : AnAction() {

    private var state: PluginState? = PluginSettings.getInstance().state


    override fun actionPerformed(e: AnActionEvent) {
        val fileChooserDescriptor = FileChooserDescriptor(
            true, false, false, false, false, false
        )

        fileChooserDescriptor.title = "Choose PyEnv executable Path"

        fileChooserDescriptor.chooseFile(e) {
            state!!.pathToPyenv = it.path
            Messages.showMessageDialog(e.project, "Choosed path${it.path}", "Path", Messages.getInformationIcon())
        }
    }
}