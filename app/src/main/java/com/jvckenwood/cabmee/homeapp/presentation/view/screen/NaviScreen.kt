package com.jvckenwood.cabmee.homeapp.presentation.view.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jvckenwood.cabmee.homeapp.presentation.theme.HomeAppTheme
import com.jvckenwood.cabmee.homeapp.presentation.viewmodel.HiddenAction
import com.jvckenwood.cabmee.homeapp.presentation.viewmodel.MainViewModel

enum class RouteScreen {
    MAIN_SCREEN,
    SETTING_SCREEN
}

@Composable
fun NaviScreen(vm: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current
    val autoStartAppOptions by vm.autoStartAppOptions.collectAsState()
    val selectedAutoStartAppIndex by vm.selectedAutoStartAppIndex.collectAsState()
    val selectedAutoStartInterval by vm.selectedAutoStartInterval.collectAsState()

    LaunchedEffect(Unit) {
        vm.initialize()
    }

    HomeAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = RouteScreen.MAIN_SCREEN.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = RouteScreen.MAIN_SCREEN.name) {
                    MainScreen(
                        uiState = uiState,
                        onLaunchPackage = { packageName ->
                            vm.launchPackage(packageName)
                        },
                        onHiddenTap = { code ->
                            vm.registerHiddenTap(code)
                        },
                        onMessage = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        onHiddenAction = { action ->
                            when (action) {
                                is HiddenAction.LaunchPackage -> vm.launchPackage(action.packageName)
                                HiddenAction.OpenSettingsScreen -> {
                                    navController.navigate(RouteScreen.SETTING_SCREEN.name)
                                }

                                null -> Unit
                            }
                        }
                    )
                }
                composable(route = RouteScreen.SETTING_SCREEN.name) {
                    SettingScreen(
                        autoStartAppOptions = autoStartAppOptions,
                        selectedAutoStartAppIndex = selectedAutoStartAppIndex,
                        autoStartIntervalOptions = vm.autoStartIntervalSecondsOptions,
                        selectedAutoStartInterval = selectedAutoStartInterval,
                        onAutoStartAppSelected = { selectedIndex ->
                            vm.updateAutoStartSettings(selectedIndex, selectedAutoStartInterval)
                        },
                        onAutoStartIntervalSelected = { selectedInterval ->
                            vm.updateAutoStartSettings(selectedAutoStartAppIndex, selectedInterval)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, apiLevel = 33)
@Composable
fun NaviScreenPreview() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "NaviScreen")
    }
}
