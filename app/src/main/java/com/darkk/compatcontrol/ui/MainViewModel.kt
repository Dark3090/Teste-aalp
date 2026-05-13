package com.darkk.compatcontrol.ui

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.darkk.compatcontrol.data.AppConfig
import com.darkk.compatcontrol.data.AppInfo
import com.darkk.compatcontrol.data.ConfigRepository
import com.darkk.compatcontrol.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UiState(
    val isRootAvailable: Boolean = false,
    val isLoading: Boolean = true,
    val apps: List<AppInfo> = emptyList(),
    val configuredApps: Set<String> = emptySet(),
    val searchQuery: String = "",
    val showSystemApps: Boolean = false,
    val snackbarMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ConfigRepository(application)
    private val pm = application.packageManager

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        checkRoot()
        loadApps()
    }

    private fun checkRoot() {
        viewModelScope.launch {
            val hasRoot = withContext(Dispatchers.IO) { RootManager.isRootAvailable() }
            _uiState.value = _uiState.value.copy(isRootAvailable = hasRoot)
        }
    }

    fun loadApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val apps = withContext(Dispatchers.IO) {
                pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { it.packageName != "com.darkk.compatcontrol" }
                    .map { appInfo ->
                        AppInfo(
                            packageName = appInfo.packageName,
                            label = pm.getApplicationLabel(appInfo).toString(),
                            isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        )
                    }
                    .sortedWith(compareBy({ it.isSystemApp }, { it.label.lowercase() }))
            }
            val configured = repo.loadAllConfigs().keys
            _uiState.value = _uiState.value.copy(
                apps = apps,
                configuredApps = configured,
                isLoading = false
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleSystemApps() {
        _uiState.value = _uiState.value.copy(
            showSystemApps = !_uiState.value.showSystemApps
        )
    }

    fun filteredApps(): List<AppInfo> {
        val state = _uiState.value
        return state.apps.filter { app ->
            (state.showSystemApps || !app.isSystemApp) &&
            (state.searchQuery.isBlank() ||
             app.label.contains(state.searchQuery, ignoreCase = true) ||
             app.packageName.contains(state.searchQuery, ignoreCase = true))
        }
    }

    fun loadConfig(packageName: String): AppConfig = repo.loadConfig(packageName)

    fun applyConfig(config: AppConfig) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { RootManager.applyConfig(config) }
            if (result.isSuccess) {
                repo.saveConfig(config)
                _uiState.value = _uiState.value.copy(
                    configuredApps = _uiState.value.configuredApps + config.packageName,
                    snackbarMessage = "Configuração aplicada!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "Erro: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun resetConfig(packageName: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { RootManager.resetConfig(packageName) }
            if (result.isSuccess) {
                repo.deleteConfig(packageName)
                _uiState.value = _uiState.value.copy(
                    configuredApps = _uiState.value.configuredApps - packageName,
                    snackbarMessage = "Configuração resetada!"
                )
            }
        }
    }

    fun isAppRunning(packageName: String): Boolean = RootManager.isAppRunning(packageName)

    fun getMemInfo(packageName: String): String? = RootManager.getMemInfo(packageName)

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }
}
