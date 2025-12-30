package com.v.namingx

import com.intellij.icons.AllIcons
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.v.namingx.service.GeneratorService
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.SwingUtilities

class AppSettingsConfigurable : Configurable {

    private var settings = AppSettings.instance
    private var panel: DialogPanel? = null

    // Capture fields to test with current input
    private lateinit var apiUrlField: JBTextField
    private lateinit var apiKeyField: JBTextField
    private lateinit var modelField: JBTextField

    override fun getDisplayName(): String = "NamingX"

    override fun createComponent(): JComponent {
        panel = panel {
            row("API URL:") {
                apiUrlField = textField()
                    .bindText(settings::apiUrl)
                    .comment("e.g. https://api.openai.com/v1/chat/completions")
                    .component
            }
            row("API Key:") {
                apiKeyField = textField()
                    .bindText(settings::apiKey)
                    .comment("Enter your OpenAI API Key here.")
                    .component
            }
            row("Model Name:") {
                modelField = textField()
                    .bindText(settings::modelName)
                    .comment("e.g. gpt-5.2, gemini-3-pro-preview")
                    .component
            }

            separator()

            row {
                val statusLabel = JLabel("")

                button("Test Connection") {
                    val url = apiUrlField.text.trim()
                    val key = apiKeyField.text.trim()
                    val model = modelField.text.trim()

                    statusLabel.text = "Testing..."
                    statusLabel.icon = AllIcons.Process.Step_1
                    statusLabel.foreground = JBColor.GRAY

                    val service = GeneratorService()
                    service.testConnection(key, url, model) { success, msg ->
                        SwingUtilities.invokeLater {
                            if (success) {
                                statusLabel.text = "Success: $msg"
                                statusLabel.icon = AllIcons.General.InspectionsOK
                                statusLabel.foreground = JBColor.GREEN
                            } else {
                                statusLabel.text = "Failed: $msg"
                                statusLabel.icon = AllIcons.General.Error
                                statusLabel.foreground = JBColor.RED
                            }
                        }
                    }
                }

                cell(statusLabel)
            }
        }
        return panel!!
    }

    override fun isModified(): Boolean {
        return panel?.isModified() ?: false
    }

    override fun apply() {
        panel?.apply()
    }

    override fun reset() {
        panel?.reset()
    }

    override fun disposeUIResources() {
        panel = null
    }
}
