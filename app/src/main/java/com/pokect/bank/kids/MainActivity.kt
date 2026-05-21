package com.pokect.bank.kids

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pokect.bank.kids.di.AppContainer
import com.pokect.bank.kids.ui.navigation.PokectBankNavGraph
import com.pokect.bank.kids.ui.navigation.Screen
import com.pokect.bank.kids.ui.navigation.bottomNavTabs
import com.pokect.bank.kids.ui.screens.login.LoginViewModel
import com.pokect.bank.kids.ui.theme.PokectBankTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AppContainer with application context for DataStore
        AppContainer.init(applicationContext)

        setContent {
            PokectBankTheme {
                val loginViewModel: LoginViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return LoginViewModel(AppContainer.loginPreferences) as T
                        }
                    }
                )
                val authState by loginViewModel.uiState.collectAsStateWithLifecycle()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Three-state auth gate: checking → logged out → logged in
                if (authState.isCheckingLogin) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Verificando...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Scaffold(
                        bottomBar = {
                            if (currentRoute != Screen.Login.route) {
                                NavigationBar {
                                    bottomNavTabs.forEach { tab ->
                                        val selected = currentRoute == tab.route
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = {
                                                navController.navigate(tab.route) {
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            icon = {
                                                Box {
                                                    Icon(tab.icon, contentDescription = tab.title)
                                                    if (tab == Screen.Missions) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .offset(
                                                                    x = 10.dp,
                                                                    y = (-4).dp
                                                                )
                                                                .background(MaterialTheme.colorScheme.error, CircleShape)
                                                        )
                                                    }
                                                }
                                            },
                                            label = { Text(tab.title, style = MaterialTheme.typography.labelSmall) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        PokectBankNavGraph(
                            navController = navController,
                            isLoggedIn = authState.isLoggedIn,
                            container = AppContainer,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
