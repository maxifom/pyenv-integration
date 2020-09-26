package com.maxifom.pyenv_integration

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class ApplicationConfigurable : Configurable {
    var settingsForm: SettingsForm? = null

    override fun createComponent(): JComponent? {
        settingsForm = settingsForm ?: SettingsForm()
        settingsForm?.loadSettings()
        return settingsForm?.component()
    }

    override fun isModified(): Boolean {
        return false
    }

    override fun apply() {
        return
    }

    override fun getDisplayName(): String {
        return "PyEnv Integration"
    }

    override fun disposeUIResources() {
        settingsForm = null
    }
}