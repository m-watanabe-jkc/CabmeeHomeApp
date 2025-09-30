package com.jvckenwood.cabmee.homeapp.presentation.view.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jvckenwood.cabmee.homeapp.presentation.theme.HomeAppTheme
import com.jvckenwood.cabmee.homeapp.presentation.viewmodel.MainViewModel

enum class RouteScreen {
    MAIN_SCREEN,
    SETTING_SCREEN
}

@Composable
fun NaviScreen(
    vm: MainViewModel
) {
    val navController = rememberNavController()
    vm.initialize()
    val counter by vm.counter.collectAsState()
    HomeAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = RouteScreen.MAIN_SCREEN.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = RouteScreen.MAIN_SCREEN.name) {
                    MainScreen(
                        vm,
                        onMessage = { msg ->
                            // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        },
                        onOpenSettings = {
                            navController.navigate(RouteScreen.SETTING_SCREEN.name)
                        }
                    )
                }
                composable(route = RouteScreen.SETTING_SCREEN.name) {
                    SettingScreen(
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
