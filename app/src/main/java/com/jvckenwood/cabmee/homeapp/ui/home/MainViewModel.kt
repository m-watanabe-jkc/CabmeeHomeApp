package com.jvckenwood.cabmee.homeapp.ui.home

import android.app.Application
import android.graphics.drawable.Drawable
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jvckenwood.cabmee.homeapp.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppEntryUiModel(
    val packageName: String,
    val icon: Drawable,
    val label: String
)

data class HomeUiState(
    val slots: List<String?> = emptyList(),
    val appEntries: Map<String, AppEntryUiModel> = emptyMap(),
    val versionText: String = BuildConfig.VERSION_NAME
)

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val targetPackageList = listOf(
        "com.jvckenwood.taitis.taitiscarapp",
        "com.ubercab.driver",
        "com.jvckenwood.taitis.cabmeekeyboard",
        "com.jvckenwood.taitis.carappupdater",
        "com.jvckenwood.carappupdater",
        "com.android.calculator2"
    )

    private val _uiState = MutableStateFlow(
        HomeUiState(
            slots = buildSlots(targetPackageList)
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val taps = mutableListOf<String>()
    private var startMs = 0L

    init {
        loadAppEntries()
    }

    private fun buildSlots(packages: List<String>): List<String?> {
        val trimmed = packages.take(10)
        return trimmed + List(10 - trimmed.size) { null }
    }

    private fun loadAppEntries() {
        val context = getApplication<Application>()
        val pm = context.packageManager
        viewModelScope.launch {
            val loaded = uiState.value.slots
                .filterNotNull()
                .distinct()
                .mapNotNull { packageName ->
                    runCatching {
                        val appInfo = pm.getApplicationInfo(packageName, 0)
                        val icon = pm.getApplicationIcon(appInfo)
                        val label = pm.getApplicationLabel(appInfo).toString()
                        packageName to AppEntryUiModel(packageName, icon, label)
                    }.getOrNull()
                }
                .toMap()
            _uiState.value = _uiState.value.copy(appEntries = loaded)
        }
    }

    fun registerHiddenTap(code: String): String? {
        val now = SystemClock.elapsedRealtime()

        if (taps.isEmpty() || (now - startMs) > 3000L) {
            taps.clear()
            startMs = now
        }

        taps.add(code)
        if (taps.size > 7) {
            taps.clear()
            startMs = 0L
            return null
        }

        if (taps.size == 7) {
            val sequence = taps.toList()
            taps.clear()
            startMs = 0L
            return when (sequence) {
                listOf("LT", "LT", "RB", "RB", "LT", "RB", "RB") -> "com.android.settings"
                listOf("RB", "RB", "LT", "LT", "RB", "LT", "LT") -> "com.cyanogenmod.filemanager"
                else -> null
            }
        }

        return null
    }
}
