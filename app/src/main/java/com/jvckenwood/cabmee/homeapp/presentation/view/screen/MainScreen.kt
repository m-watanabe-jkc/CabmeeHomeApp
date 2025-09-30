package com.jvckenwood.cabmee.homeapp.presentation.view.screen

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.PowerManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.jvckenwood.cabmee.homeapp.presentation.viewmodel.HiddenAction
import com.jvckenwood.cabmee.homeapp.presentation.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onMessage: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val config = LocalConfiguration.current
    val uiState by viewModel.uiState.collectAsState()
    var showRebootDialog by remember { mutableStateOf(false) }
    val noRipple = remember { MutableInteractionSource() }

    val columns = if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) 5 else 2

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "APPLICATIONS",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 2.dp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = false
            ) {
                itemsIndexed(uiState.slots) { _, packageName ->
                    if (packageName == null) {
                        EmptySlot()
                    } else {
                        val entry = uiState.appEntries[packageName]
                        if (entry == null) {
                            EmptySlot()
                        } else {
                            AppItemSlot(
                                icon = entry.icon,
                                label = entry.label,
                                onClick = {
                                    val intent = pm.getLaunchIntentForPackage(packageName)
                                    if (intent != null) {
                                        context.startActivity(intent)
                                    } else {
                                        onMessage("起動できません: $packageName")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showRebootDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.RestartAlt,
                contentDescription = "Reboot"
            )
        }

        Text(
            text = "v${uiState.versionText}",
            fontSize = 12.sp,
            color = Color.DarkGray,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 12.dp)
        )

        HiddenCornerButton(
            alignment = Alignment.TopStart,
            noRipple = noRipple
        ) {
            handleHiddenAction(
                context = context,
                action = viewModel.registerHiddenTap("LT"),
                onMessage = onMessage,
                onOpenSettings = onOpenSettings
            )
        }

        HiddenCornerButton(
            alignment = Alignment.TopEnd,
            noRipple = noRipple
        ) {
            handleHiddenAction(
                context = context,
                action = viewModel.registerHiddenTap("RT"),
                onMessage = onMessage,
                onOpenSettings = onOpenSettings
            )
        }

        HiddenCornerButton(
            alignment = Alignment.BottomStart,
            noRipple = noRipple
        ) {
            handleHiddenAction(
                context = context,
                action = viewModel.registerHiddenTap("LB"),
                onMessage = onMessage,
                onOpenSettings = onOpenSettings
            )
        }

        HiddenCornerButton(
            alignment = Alignment.BottomEnd,
            noRipple = noRipple
        ) {
            handleHiddenAction(
                context = context,
                action = viewModel.registerHiddenTap("RB"),
                onMessage = onMessage,
                onOpenSettings = onOpenSettings
            )
        }

        if (showRebootDialog) {
            AlertDialog(
                onDismissRequest = { showRebootDialog = false },
                title = { Text("確認") },
                text = { Text("再起動します。よろしいですか？") },
                dismissButton = {
                    TextButton(onClick = { showRebootDialog = false }) {
                        Text("キャンセル")
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showRebootDialog = false
                            try {
                                val powerManager =
                                    context.getSystemService(Context.POWER_SERVICE) as PowerManager
                                powerManager.reboot(null)
                            } catch (_: SecurityException) {
                                onMessage("再起動権限がありません（端末/権限要件を確認してください）")
                            } catch (e: Exception) {
                                onMessage("再起動に失敗しました: ${e.message}")
                            }
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun BoxScope.HiddenCornerButton(
    alignment: Alignment,
    noRipple: MutableInteractionSource,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .align(alignment)
            .size(72.dp)
            .background(Color.Transparent)
            .clickable(
                interactionSource = noRipple,
                indication = null,
                onClick = onClick
            )
    )
}

private fun handleHiddenAction(
    context: Context,
    action: HiddenAction?,
    onMessage: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    when (action) {
        is HiddenAction.LaunchPackage -> launchHiddenPackage(
            context = context,
            packageName = action.packageName,
            onMessage = onMessage
        )

        HiddenAction.OpenSettingsScreen -> onOpenSettings()
        null -> Unit
    }
}

private fun launchHiddenPackage(
    context: Context,
    packageName: String,
    onMessage: (String) -> Unit
) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
    } else {
        onMessage("起動できません（未インストール/起動不可）: $packageName")
    }
}

@Composable
private fun AppItemSlot(
    icon: android.graphics.drawable.Drawable,
    label: String,
    onClick: () -> Unit
) {
    val bmp = remember(icon) { icon.toBitmap().asImageBitmap() }

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            bitmap = bmp,
            contentDescription = label,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EmptySlot() {
    Box(modifier = Modifier.aspectRatio(1f))
}

// @Preview(showBackground = true, apiLevel = 33)
// @Composable
// fun Screen2Preview() {
//    MainScreen(
//        viewModel = MainViewModel(),
//        onMessage = {},
//        onOpenSettings = {}
//    )
// }
