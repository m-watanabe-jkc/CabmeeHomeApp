package com.jvckenwood.cabmee.homeapp.presentation.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvckenwood.cabmee.homeapp.BuildConfig
import com.jvckenwood.cabmee.homeapp.domain.entity.StateManager
import com.jvckenwood.cabmee.homeapp.domain.state.MainState
import com.jvckenwood.cabmee.homeapp.domain.usecase.InitializeUseCase
import com.jvckenwood.cabmee.homeapp.domain.usecase.UpdateAutoStartAppSettingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class AppEntryUiModel(
    val packageName: String,
    val icon: Drawable,
    val label: String
)

data class AutoStartAppOption(
    val index: Int?,
    val label: String
)

data class InstalledAppUiModel(
    val packageName: String,
    val label: String
)

data class HomeUiState(
    val slots: List<String?> = emptyList(),
    val appEntries: Map<String, AppEntryUiModel> = emptyMap(),
    val canReboot: Boolean = false,
    val autoStartPackageName: String? = null,
    val autoStartIntervalSeconds: Int = 30,
    val versionText: String = BuildConfig.VERSION_NAME
)

sealed interface HiddenAction {
    data class LaunchPackage(val packageName: String) : HiddenAction
    data object OpenSettingsScreen : HiddenAction
}

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val initializeUseCase: InitializeUseCase,
    private val updateAutoStartAppSettingUseCase: UpdateAutoStartAppSettingUseCase,
    private val stateMgr: StateManager
) : ViewModel() {
    private val targetPackageList = listOf(
        "com.jvckenwood.taitis.taitiscarapp",
        "com.ubercab.driver",
        "com.jvckenwood.taitis.cabmeekeyboard",
        "com.jvckenwood.taitis.carappupdater",
        "com.jvckenwood.carappupdater",
        "com.android.calculator2"
    )

    private val autoStartIntervalOptions = listOf(5, 10, 20, 30, 60)

    private val _uiState = MutableStateFlow(
        HomeUiState(
            slots = buildSlots(targetPackageList),
            canReboot = context.checkSelfPermission(android.Manifest.permission.REBOOT) == PackageManager.PERMISSION_GRANTED
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _autoStartAppOptions = MutableStateFlow<List<AutoStartAppOption>>(emptyList())
    val autoStartAppOptions: StateFlow<List<AutoStartAppOption>> = _autoStartAppOptions.asStateFlow()

    val autoStartIntervalSecondsOptions: List<Int> = autoStartIntervalOptions

    private val _selectedAutoStartAppIndex = MutableStateFlow<Int?>(null)
    val selectedAutoStartAppIndex: StateFlow<Int?> = _selectedAutoStartAppIndex.asStateFlow()

    private val _selectedAutoStartInterval = MutableStateFlow(30)
    val selectedAutoStartInterval: StateFlow<Int> = _selectedAutoStartInterval.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppUiModel>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppUiModel>> = _installedApps.asStateFlow()

    private val taps = mutableListOf<String>()
    private var startMs = 0L

    init {
        loadAppEntries()
        loadInstalledApps()
        observeMainState()
    }

    fun initialize() {
        viewModelScope.launch {
            initializeUseCase()
        }
    }

    private fun observeMainState() {
        stateMgr.mainState
            .onEach { new ->
                if (new is MainState.Success) {
                    val data = new.mainData
                    _selectedAutoStartAppIndex.value = data.autoStartApplicationIndex.takeIf { it >= 0 }
                    _selectedAutoStartInterval.value = data.autoStartApplicationInterval
                        .takeIf { it in autoStartIntervalOptions }
                        ?: 30
                    _uiState.value = _uiState.value.copy(
                        autoStartPackageName = data.autoStartApplicationIndex
                            .takeIf { it in targetPackageList.indices }
                            ?.let { targetPackageList[it] },
                        autoStartIntervalSeconds = data.autoStartApplicationInterval
                            .takeIf { it in autoStartIntervalOptions }
                            ?: 30
                    )
                    Timber.i("NEW COUNTER: ${data.counter}")
                }
            }
            .launchIn(viewModelScope)
    }

    private fun buildSlots(packages: List<String>): List<String?> {
        val trimmed = packages.take(10)
        return trimmed + List(10 - trimmed.size) { null }
    }

    private fun loadAppEntries() {
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
            _autoStartAppOptions.value = buildAutoStartOptions()
        }
    }

    private fun buildAutoStartOptions(): List<AutoStartAppOption> {
        val options = mutableListOf(AutoStartAppOption(index = null, label = "無し"))
        targetPackageList.forEachIndexed { index, packageName ->
            val label = uiState.value.appEntries[packageName]?.label ?: packageName
            options += AutoStartAppOption(index = index, label = label)
        }
        return options
    }

    fun updateAutoStartSettings(autoStartAppIndex: Int?, intervalSeconds: Int) {
        val normalizedIndex = autoStartAppIndex ?: -1
        val normalizedInterval = intervalSeconds.takeIf { it in autoStartIntervalOptions } ?: 30
        viewModelScope.launch {
            updateAutoStartAppSettingUseCase(
                autoStartApplicationIndex = normalizedIndex,
                autoStartApplicationInterval = normalizedInterval
            )
        }
    }

    private fun loadInstalledApps() {
        val pm = context.packageManager
        viewModelScope.launch {
            val apps = runCatching {
                pm.getInstalledApplications(0)
                    .asSequence()
                    .map { appInfo ->
                        InstalledAppUiModel(
                            packageName = appInfo.packageName,
                            label = pm.getApplicationLabel(appInfo).toString()
                        )
                    }
                    .filterNot { app -> isBlacklistedPackage(app.packageName) }
                    .sortedBy { it.label.lowercase() }
                    .toList()
            }.getOrDefault(emptyList())

            _installedApps.value = apps
        }
    }

    private fun isBlacklistedPackage(packageName: String): Boolean {
        return BLACKLIST_PATTERNS.any { pattern -> wildcardMatch(packageName, pattern) }
    }

    private fun wildcardMatch(value: String, pattern: String): Boolean {
        val regex = pattern
            .replace(".", "\\.")
            .replace("*", ".*")
            .toRegex()
        return regex.matches(value)
    }

    fun registerHiddenTap(code: String): HiddenAction? {
        val now = SystemClock.elapsedRealtime()

        if (taps.isEmpty() || (now - startMs) > 3000L) {
            taps.clear()
            startMs = now
        }

        taps.add(code)

        if (taps == listOf("LT", "RT", "LB", "RB")) {
            taps.clear()
            startMs = 0L
            return HiddenAction.OpenSettingsScreen
        }

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
                listOf("LT", "LT", "RB", "RB", "LT", "RB", "RB") -> {
                    HiddenAction.LaunchPackage("com.android.settings")
                }

                listOf("RB", "RB", "LT", "LT", "RB", "LT", "LT") -> {
                    HiddenAction.LaunchPackage("com.cyanogenmod.filemanager")
                }

                else -> null
            }
        }

        return null
    }

    fun launchPackage(packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        return true
    }

    companion object {
        // 例: listOf("com.example.*", "com.android.systemui")
        private val BLACKLIST_PATTERNS: List<String> = emptyList()
    }
}
