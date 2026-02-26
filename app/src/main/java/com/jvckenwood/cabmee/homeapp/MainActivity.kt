package com.jvckenwood.cabmee.homeapp

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.PowerManager
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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

class MainActivity : ComponentActivity() {

    // 最大10個まで
    private val targetPackageList = arrayListOf(
        "com.jvckenwood.taitis.taitiscarapp",
        "com.ubercab.driver",
        "com.jvckenwood.taitis.cabmeekeyboard",
        "com.jvckenwood.taitis.carappupdater",
        "com.jvckenwood.carappupdater",
        "com.android.calculator2"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 戻る無効（ホームとして）
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // 何もしない
                }
            }
        )

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppGridHome(
                        targetPackages = targetPackageList,
                        onMessage = { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
                    )
                }
            }
        }
    }
}

@Composable
fun AppGridHome(
    targetPackages: List<String>,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val config = LocalConfiguration.current
    var showRebootDialog by remember { mutableStateOf(false) }
    val versionText = remember { BuildConfig.VERSION_NAME }
    val noRipple = remember { MutableInteractionSource() }
    val taps = remember { mutableStateListOf<String>() }
    var startMs by remember { mutableLongStateOf(0L) }

    // 横=5列、縦=2列
    val columns = if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) 5 else 2

    // 常に10枠。足りない分は null で埋める（右下側が空白になる）
    val slots: List<String?> = remember(targetPackages) {
        val trimmed = targetPackages.take(10)
        trimmed + List(10 - trimmed.size) { null }
    }

    data class AppEntry(
        val icon: Drawable,
        val label: String
    )

    // アイコンをキャッシュ（packageName -> Drawable?）
    val appCache = remember { mutableStateMapOf<String, AppEntry?>() }

    fun registerTap(code: String) {
        val now = SystemClock.elapsedRealtime()

        // 1回目 or タイムアウトしたら最初から
        if (taps.isEmpty() || (now - startMs) > 3000L) {
            taps.clear()
            startMs = now
        }
        taps.add(code)
        if (taps.size > 7) {
            taps.clear()
            startMs = 0L
            return
        } else if (taps.size == 7) {
            when (taps.toList()) {
                listOf("LT", "LT", "RB", "RB", "LT", "RB", "RB") -> {
                    val pkg = "com.android.settings"
                    val intent = pm.getLaunchIntentForPackage(pkg)
                    if (intent != null) {
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } else {
                        onMessage("起動できません（未インストール/起動不可）: $pkg")
                    }
                }
                listOf("RB", "RB", "LT", "LT", "RB", "LT", "LT") -> {
                    val pkg = "com.cyanogenmod.filemanager"
                    val intent = pm.getLaunchIntentForPackage(pkg)
                    if (intent != null) {
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } else {
                        onMessage("起動できません（未インストール/起動不可）: $pkg")
                    }
                }
            }
        }
    }

    // 表示対象（null以外）のアイコンをまとめて取得
    LaunchedEffect(slots) {
        slots.filterNotNull().distinct().forEach { pkg ->
            if (!appCache.containsKey(pkg)) {
                appCache[pkg] = try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    val icon = pm.getApplicationIcon(appInfo)
                    val label = pm.getApplicationLabel(appInfo).toString()
                    AppEntry(icon, label)
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

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
            // タイトル表示
            Text(
                text = "APPLICATIONS",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            // 区切り線
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 2.dp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            // アイコンを表示
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = false // 10個固定ならスクロール不要
            ) {
                itemsIndexed(slots) { _, pkg ->
                    if (pkg == null) {
                        EmptySlot()
                    } else {
                        val entry = appCache[pkg]
                        if (entry == null) {
                            EmptySlot()
                        } else {
                            AppItemSlot(
                                icon = entry.icon,
                                label = entry.label,
                                onClick = {
                                    val intent = pm.getLaunchIntentForPackage(pkg)
                                    if (intent != null) {
                                        context.startActivity(intent)
                                    } else {
                                        onMessage("起動できません: $pkg")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        // 画面中央下部のリブートボタンを配置
        FloatingActionButton(
            onClick = { showRebootDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                // バージョン表示やジェスチャーバーと被らないよう少し上げる
                .padding(bottom = 28.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.RestartAlt,
                contentDescription = "Reboot"
            )
        }
        // 画面右下にバージョンを表示
        Text(
            text = "v$versionText",
            fontSize = 12.sp,
            color = Color.DarkGray,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 12.dp)
        )
        // 左上隠しボタン
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(72.dp)
                .background(Color.Transparent)
                .clickable(
                    interactionSource = noRipple,
                    indication = null
                ) { registerTap("LT") }
        )
        // 右下隠しボタン
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(72.dp)
                .background(Color.Transparent)
                .clickable(
                    interactionSource = noRipple,
                    indication = null
                ) { registerTap("RB") }
        )
        // リブート確認ダイアログ
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
                                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                                pm.reboot(null)
                            } catch (e: SecurityException) {
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
private fun AppItemSlot(
    icon: Drawable,
    label: String,
    onClick: () -> Unit
) {
    val bmp = remember(icon) { icon.toBitmap().asImageBitmap() }

    Column(
        modifier = Modifier
            .aspectRatio(1f) // マスを正方形に
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
    // 何も描画しない（ただしマスの形は維持したいのでBoxだけ置く）
    Box(
        modifier = Modifier.aspectRatio(1f)
    )
}
