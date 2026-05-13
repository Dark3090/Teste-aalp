package com.darkk.compatcontrol.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ConfigRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("compat_configs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveConfig(config: AppConfig) {
        val json = gson.toJson(config)
        prefs.edit().putString(config.packageName, json).apply()
    }

    fun loadConfig(packageName: String): AppConfig {
        val json = prefs.getString(packageName, null) ?: return AppConfig(packageName)
        return try {
            gson.fromJson(json, AppConfig::class.java)
        } catch (e: Exception) {
            AppConfig(packageName)
        }
    }

    fun loadAllConfigs(): Map<String, AppConfig> {
        val type = object : TypeToken<AppConfig>() {}.type
        return prefs.all.mapNotNull { (key, value) ->
            try {
                val config = gson.fromJson(value as String, AppConfig::class.java)
                key to config
            } catch (e: Exception) { null }
        }.toMap()
    }

    fun deleteConfig(packageName: String) {
        prefs.edit().remove(packageName).apply()
    }

    fun hasConfig(packageName: String): Boolean {
        return prefs.contains(packageName)
    }
}
