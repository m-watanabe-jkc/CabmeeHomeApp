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

data class HomeUiState(
    val slots: List<String?> = emptyList(),
    val appEntries: Map<String, AppEntryUiModel> = emptyMap(),
    val canReboot: Boolean = false,
    val autoStartPackageName: String? = null,
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
    stateMgr: StateManager
) : ViewModel() {
    private val targetPackageList = listOf(
        "com.jvckenwood.taitis.taitiscarapp",
        "com.ubercab.driver",
        "com.jvckenwood.taitis.cabmeekeyboard",
        "com.jvckenwood.taitis.carappupdater",
        "com.jvckenwood.carappupdater",
        "com.android.calculator2"
    )

    // Nullable: null の場合は自動起動しない
    private val autioStartApplicationIndex: Int? = 0

    private val _uiState = MutableStateFlow(
        HomeUiState(
            slots = buildSlots(targetPackageList),
            canReboot = context.checkSelfPermission(android.Manifest.permission.REBOOT) == PackageManager.PERMISSION_GRANTED,
            autoStartPackageName = autioStartApplicationIndex
                ?.takeIf { it in targetPackageList.indices }
                ?.let { targetPackageList[it] }
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val taps = mutableListOf<String>()
    private var startMs = 0L

    init {
        loadAppEntries()
        stateMgr.mainState
            .onEach { new ->
                if (new is MainState.Success) {
                    val data = new.mainData
                    Timber.i("NEW COUNTER: ${data.counter}")
                }
            }.launchIn(viewModelScope)
    }

    fun initialize() {
        viewModelScope.launch {
            initializeUseCase()
        }
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
        }
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
}
