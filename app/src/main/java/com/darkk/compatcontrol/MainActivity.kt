package com.darkk.compatcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.darkk.compatcontrol.root.RootManager
import com.darkk.compatcontrol.ui.MainViewModel
import com.darkk.compatcontrol.ui.screens.AppConfigScreen
import com.darkk.compatcontrol.ui.screens.AppListScreen
import com.darkk.compatcontrol.ui.theme.CompatControlTheme
import com.darkk.compatcontrol.ui.theme.BgDark
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RootManager.init()

        setContent {
            CompatControlTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                val state by viewModel.uiState.collectAsState()

                // Show snackbar on messages
                LaunchedEffect(state.snackbarMessage) {
                    state.snackbarMessage?.let { msg ->
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                        viewModel.clearSnackbar()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = BgDark,
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { _ ->
                    NavHost(navController = navController, startDestination = "list") {
                        composable("list") {
                            AppListScreen(
                                viewModel = viewModel,
                                onAppClick = { pkg ->
                                    navController.navigate("config/$pkg")
                                }
                            )
                        }
                        composable("config/{packageName}") { backStackEntry ->
                            val pkg = backStackEntry.arguments?.getString("packageName") ?: return@composable
                            AppConfigScreen(
                                packageName = pkg,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
