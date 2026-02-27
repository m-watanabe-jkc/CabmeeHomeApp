package com.jvckenwood.cabmee.homeapp.presentation.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jvckenwood.cabmee.homeapp.presentation.view.screen.NaviScreen
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
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
        enableEdgeToEdge()
        setContent {
            NaviScreen()
        }
    }
}
