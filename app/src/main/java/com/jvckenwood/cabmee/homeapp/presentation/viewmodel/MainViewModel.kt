package com.jvckenwood.cabmee.homeapp.presentation.viewmodel

import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
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
import com.jvckenwood.cabmee.homeapp.domain.usecase.UpdateTargetPackageUseCase
import com.jvckenwood.cabmee.homeapp.domain.usecase.UpdateViewingMonitoringSettingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val label: String,
    val icon: Drawable
)

data class HomeUiState(
    val slots: List<String?> = emptyList(),
    val appEntries: Map<String, AppEntryUiModel> = emptyMap(),
    val canReboot: Boolean = false,
    val autoStartPackageName: String? = null,
    val autoStartIntervalSeconds: Int = 30,
    val viewingMonitoringMode: Boolean = false,
    val versionText: String = BuildConfig.VERSION_NAME
)

sealed interface HiddenAction {
    data class LaunchPackage(val packageName: String) : HiddenAction
    data object OpenSettingsScreen : HiddenAction
}

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val initializeUseCase: InitializeUseCase,
    private val updateAutoStartAppSettingUseCase: UpdateAutoStartAppSettingUseCase,
    private val updateTargetPackageUseCase: UpdateTargetPackageUseCase,
    private val updateViewingMonitoringSettingUseCase: UpdateViewingMonitoringSettingUseCase,
    private val stateMgr: StateManager
) : ViewModel() {
    private val autoStartIntervalOptions = listOf(5, 10, 20, 30, 60)

    private val _uiState = MutableStateFlow(
        HomeUiState(
            slots = List(10) { null },
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

    private val _targetPackageSelections = MutableStateFlow<Map<String, Int?>>(emptyMap())
    val targetPackageSelections: StateFlow<Map<String, Int?>> = _targetPackageSelections.asStateFlow()

    private val _viewingMonitoringMode = MutableStateFlow(false)
    val viewingMonitoringMode: StateFlow<Boolean> = _viewingMonitoringMode.asStateFlow()

    private val taps = mutableListOf<String>()
    private var startMs = 0L
    private var viewingMonitorJob: Job? = null

    init {
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
                    val normalizedTargets = normalizeTargetList(data.targetPackageList)
                    _selectedAutoStartAppIndex.value = data.autoStartApplicationIndex.takeIf { it >= 0 }
                    _selectedAutoStartInterval.value = data.autoStartApplicationInterval.takeIf { it in autoStartIntervalOptions } ?: 30
                    _viewingMonitoringMode.value = data.viewingMonitoringMode

                    _uiState.value = _uiState.value.copy(
                        slots = normalizedTargets.map { it.takeIf { pkg -> pkg.isNotBlank() } },
                        autoStartPackageName = data.autoStartApplicationIndex
                            .takeIf { it in normalizedTargets.indices }
                            ?.let { normalizedTargets[it] }
                            ?.takeIf { it.isNotBlank() }
                            ?.takeIf { pkg -> pkg in _uiState.value.appEntries.keys },
                        autoStartIntervalSeconds = data.autoStartApplicationInterval.takeIf { it in autoStartIntervalOptions } ?: 30,
                        viewingMonitoringMode = data.viewingMonitoringMode
                    )

                    loadAppEntriesForTargets(normalizedTargets)
                    _autoStartAppOptions.value = buildAutoStartOptions(normalizedTargets)
                    _targetPackageSelections.value = buildSelectionMap(normalizedTargets)
                    configureViewingMonitoring(data.viewingMonitoringMode, data.viewingRestrictionsList)
                    Timber.i("NEW COUNTER: ${data.counter}")
                }
            }
            .launchIn(viewModelScope)
    }

    private fun configureViewingMonitoring(enabled: Boolean, restrictionPatterns: List<String>) {
        viewingMonitorJob?.cancel()
        if (!enabled) return

        viewingMonitorJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val foregroundPackage = getForegroundPackageName() ?: continue
                if (foregroundPackage == context.packageName) continue

                val blocked = restrictionPatterns.any { wildcardMatch(foregroundPackage, it) }
                if (blocked) {
                    runCatching {
                        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        am.killBackgroundProcesses(foregroundPackage)
                    }
                    val intent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    private fun getForegroundPackageName(): String? {
        val usageStats = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 3_000L
        val events = usageStats.queryEvents(beginTime, endTime)
        val event = UsageEvents.Event()
        var pkg: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                pkg = event.packageName
            }
        }
        return pkg
    }

    private fun normalizeTargetList(targets: List<String>): List<String> {
        val taken = targets.take(10)
        return taken + List(10 - taken.size) { "" }
    }

    private fun loadAppEntriesForTargets(targets: List<String>) {
        val pm = context.packageManager
        viewModelScope.launch {
            val loaded = targets
                .filter { it.isNotBlank() }
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
            _autoStartAppOptions.value = buildAutoStartOptions(targets)
        }
    }

    private fun buildAutoStartOptions(targets: List<String>): List<AutoStartAppOption> {
        val options = mutableListOf(AutoStartAppOption(index = null, label = "無し"))
        targets.forEachIndexed { index, packageName ->
            if (packageName.isNotBlank()) {
                val label = _uiState.value.appEntries[packageName]?.label ?: packageName
                options += AutoStartAppOption(index = index, label = label)
            }
        }
        return options
    }

    private fun buildSelectionMap(targets: List<String>): Map<String, Int?> {
        val map = mutableMapOf<String, Int?>()
        targets.forEachIndexed { idx, pkg -> if (pkg.isNotBlank()) map[pkg] = idx + 1 }
        return map
    }

    fun updateAutoStartSettings(autoStartAppIndex: Int?, intervalSeconds: Int) {
        val normalizedIndex = autoStartAppIndex ?: -1
        val normalizedInterval = intervalSeconds.takeIf { it in autoStartIntervalOptions } ?: 30
        viewModelScope.launch {
            updateAutoStartAppSettingUseCase(normalizedIndex, normalizedInterval)
        }
    }

    fun updateViewingMonitoringMode(enabled: Boolean) {
        val restrictions = (stateMgr.mainState.value as? MainState.Success)?.mainData?.viewingRestrictionsList ?: emptyList()
        viewModelScope.launch {
            updateViewingMonitoringSettingUseCase(restrictions, enabled)
        }
    }

    fun getSelectableSlotsFor(packageName: String): List<Int?> {
        val selectedBySelf = _targetPackageSelections.value[packageName]
        val used = _targetPackageSelections.value.filterKeys { it != packageName }.values.filterNotNull().toSet()
        return listOf(null) + (1..10).filter { it !in used || it == selectedBySelf }
    }

    fun updateTargetPackageSelection(packageName: String, slot: Int?) {
        val current = _targetPackageSelections.value.toMutableMap()
        if (slot != null) {
            current.entries.forEach { if (it.key != packageName && it.value == slot) current[it.key] = null }
        }
        current[packageName] = slot
        _targetPackageSelections.value = current

        val targetList = MutableList(10) { "" }
        current.forEach { (pkg, selectedSlot) ->
            if (selectedSlot != null && selectedSlot in 1..10) targetList[selectedSlot - 1] = pkg
        }

        viewModelScope.launch { updateTargetPackageUseCase(targetList) }
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
                            label = pm.getApplicationLabel(appInfo).toString(),
                            icon = pm.getApplicationIcon(appInfo)
                        )
                    }
                    .filterNot { isBlacklistedPackage(it.packageName) }
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
        val regex = pattern.replace(".", "\\.").replace("*", ".*").toRegex()
        return regex.matches(value)
    }

    fun registerHiddenTap(code: String): HiddenAction? {
        val now = SystemClock.elapsedRealtime()
        if (taps.isEmpty() || (now - startMs) > 3000L) {
            taps.clear(); startMs = now
        }
        taps.add(code)
        if (taps == listOf("LT", "RT", "LB", "RB")) {
            taps.clear(); startMs = 0L; return HiddenAction.OpenSettingsScreen
        }
        if (taps.size > 7) {
            taps.clear(); startMs = 0L; return null
        }
        if (taps.size == 7) {
            val sequence = taps.toList(); taps.clear(); startMs = 0L
            return when (sequence) {
                listOf("LT", "LT", "RB", "RB", "LT", "RB", "RB") -> HiddenAction.LaunchPackage("com.android.settings")
                listOf("RB", "RB", "LT", "LT", "RB", "LT", "LT") -> HiddenAction.LaunchPackage("com.cyanogenmod.filemanager")
                else -> null
            }
        }
        return null
    }

    fun launchPackage(packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        return true
    }

    companion object {
        private val BLACKLIST_PATTERNS: List<String> = emptyList()
    }
}
