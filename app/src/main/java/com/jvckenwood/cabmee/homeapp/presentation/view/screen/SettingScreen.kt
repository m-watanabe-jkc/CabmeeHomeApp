package com.jvckenwood.cabmee.homeapp.presentation.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.jvckenwood.cabmee.homeapp.presentation.viewmodel.AutoStartAppOption
import com.jvckenwood.cabmee.homeapp.presentation.viewmodel.InstalledAppUiModel

private enum class SettingTab(val title: String) {
    APP_LAUNCH("アプリ起動"),
    DISPLAY("表示設定"),
    VIEWING_RESTRICTION("表示制限")
}

@Composable
fun SettingScreen(
    autoStartAppOptions: List<AutoStartAppOption>,
    selectedAutoStartAppIndex: Int?,
    autoStartIntervalOptions: List<Int>,
    selectedAutoStartInterval: Int,
    installedApps: List<InstalledAppUiModel>,
    targetPackageSelections: Map<String, Int?>,
    slotOptionsProvider: (String) -> List<Int?>,
    onTargetPackageSlotSelected: (String, Int?) -> Unit,
    onAutoStartAppSelected: (Int?) -> Unit,
    onAutoStartIntervalSelected: (Int) -> Unit,
    viewingMonitoringMode: Boolean,
    onViewingMonitoringModeChanged: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(SettingTab.APP_LAUNCH) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.clickable(onClick = onBack)
            )
            Text(
                text = "SETTINGS",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedTab.ordinal) {
            SettingTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            SettingTab.APP_LAUNCH -> AppLaunchTabContent(
                autoStartAppOptions = autoStartAppOptions,
                selectedAutoStartAppIndex = selectedAutoStartAppIndex,
                autoStartIntervalOptions = autoStartIntervalOptions,
                selectedAutoStartInterval = selectedAutoStartInterval,
                onAutoStartAppSelected = onAutoStartAppSelected,
                onAutoStartIntervalSelected = onAutoStartIntervalSelected
            )

            SettingTab.DISPLAY -> DisplaySettingTabContent(
                installedApps = installedApps,
                targetPackageSelections = targetPackageSelections,
                slotOptionsProvider = slotOptionsProvider,
                onTargetPackageSlotSelected = onTargetPackageSlotSelected
            )

            SettingTab.VIEWING_RESTRICTION -> ViewingRestrictionTabContent(
                viewingMonitoringMode = viewingMonitoringMode,
                onViewingMonitoringModeChanged = onViewingMonitoringModeChanged
            )
        }
    }
}

@Composable
private fun AppLaunchTabContent(
    autoStartAppOptions: List<AutoStartAppOption>,
    selectedAutoStartAppIndex: Int?,
    autoStartIntervalOptions: List<Int>,
    selectedAutoStartInterval: Int,
    onAutoStartAppSelected: (Int?) -> Unit,
    onAutoStartIntervalSelected: (Int) -> Unit
) {
    val selectedAppLabel = autoStartAppOptions
        .firstOrNull { it.index == selectedAutoStartAppIndex }
        ?.label
        ?: "無し"

    SettingDropdownRow(
        title = "自動起動アプリ",
        selectedText = selectedAppLabel,
        items = autoStartAppOptions,
        itemLabel = { it.label },
        onItemSelected = { option -> onAutoStartAppSelected(option.index) }
    )

    Spacer(modifier = Modifier.height(16.dp))

    SettingDropdownRow(
        title = "自動起動時間",
        selectedText = "${selectedAutoStartInterval}秒",
        items = autoStartIntervalOptions,
        itemLabel = { "${it}秒" },
        onItemSelected = onAutoStartIntervalSelected
    )
}

@Composable
private fun DisplaySettingTabContent(
    installedApps: List<InstalledAppUiModel>,
    targetPackageSelections: Map<String, Int?>,
    slotOptionsProvider: (String) -> List<Int?>,
    onTargetPackageSlotSelected: (String, Int?) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(installedApps) { app ->
            val selectedSlot = targetPackageSelections[app.packageName]
            val slotOptions = slotOptionsProvider(app.packageName)
            val iconBitmap = remember(app.icon) { app.icon.toBitmap().asImageBitmap() }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = app.label,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = app.label,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )

                SimpleDropdownRow(
                    selectedText = selectedSlot?.toString() ?: "",
                    items = slotOptions,
                    itemLabel = { it?.toString() ?: "" },
                    onItemSelected = { onTargetPackageSlotSelected(app.packageName, it) }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
        }
    }
}


@Composable
private fun ViewingRestrictionTabContent(
    viewingMonitoringMode: Boolean,
    onViewingMonitoringModeChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "表示制限 ON/OFF",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Switch(
            checked = viewingMonitoringMode,
            onCheckedChange = onViewingMonitoringModeChanged
        )
    }
}

@Composable
private fun <T> SettingDropdownRow(
    title: String,
    selectedText: String,
    items: List<T>,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedText,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemLabel(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
private fun <T> SimpleDropdownRow(
    selectedText: String,
    items: List<T>,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .clickable { expanded = true }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = selectedText,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                text = { Text(itemLabel(item)) },
                onClick = {
                    onItemSelected(item)
                    expanded = false
                }
            )
        }
    }
}

@Preview(showBackground = true, apiLevel = 33)
@Composable
fun Screen3Preview() {
    SettingScreen(
        autoStartAppOptions = listOf(
            AutoStartAppOption(null, "無し"),
            AutoStartAppOption(0, "Sample App")
        ),
        selectedAutoStartAppIndex = 0,
        autoStartIntervalOptions = listOf(5, 10, 20, 30, 60),
        selectedAutoStartInterval = 30,
        installedApps = emptyList(),
        targetPackageSelections = mapOf("com.example.alpha" to 1),
        slotOptionsProvider = { listOf(null, 1, 2, 3) },
        onTargetPackageSlotSelected = { _, _ -> },
        onAutoStartAppSelected = {},
        onAutoStartIntervalSelected = {},
        viewingMonitoringMode = false,
        onViewingMonitoringModeChanged = {},
        onBack = {}
    )
}
