package com.jvckenwood.cabmee.homeapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jvckenwood.cabmee.homeapp.ui.home.HomeScreen
import com.jvckenwood.cabmee.homeapp.ui.home.SettingScreen

private enum class ScreenRoute {
    HOME,
    SETTINGS
}

class MainActivity : ComponentActivity() {
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
            var currentScreen by rememberSaveable { mutableStateOf(ScreenRoute.HOME) }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        ScreenRoute.HOME -> {
                            HomeScreen(
                                viewModel = viewModel(),
                                onMessage = { msg ->
                                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                                },
                                onOpenSettings = { currentScreen = ScreenRoute.SETTINGS }
                            )
                        }

                        ScreenRoute.SETTINGS -> {
                            SettingScreen(
                                onBack = { currentScreen = ScreenRoute.HOME }
                            )
                        }
                    }
                }
            }
        }
    }
}
