package com.maxifom.pyenv_integration;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SettingsForm {
    private JPanel panel1;
    private TextFieldWithBrowseButton textField1;

    private final PluginSettings settings = PluginSettings.getInstance();

    public SettingsForm() {

    }

    public JComponent component() {
        return panel1;
    }

    public void loadSettings() {
        textField1.setText(settings.getState().getPathToPyenv());
    }

    private void createUIComponents() {
        textField1 = new TextFieldWithBrowseButton(new JTextField());
        textField1.addBrowseFolderListener(new TextBrowseFolderListener(
                new FileChooserDescriptor(true, false, false, false, false, false)
        ));
    }
}
