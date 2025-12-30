package com.v.namingx

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.example.naming.AppSettings",
    storages = [Storage("AiNamingSettings.xml")]
)
class AppSettings : PersistentStateComponent<AppSettings> {

    var apiKey: String = ""
    var modelName: String = ""
    var apiUrl: String = "https://api.openai.com/v1/chat/completions"
    var namingConvention: String = "camelCase"

    override fun getState(): AppSettings {
        return this
    }

    override fun loadState(state: AppSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: AppSettings
            get() = ApplicationManager.getApplication().getService(AppSettings::class.java)
    }
}
