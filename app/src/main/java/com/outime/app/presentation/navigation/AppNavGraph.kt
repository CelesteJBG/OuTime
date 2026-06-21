package com.outime.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.outime.app.data.repository.AuthRepositoryImpl
import com.outime.app.data.repository.ServiceRepositoryImpl
import com.outime.app.presentation.screens.BusinessHomeScreen
import com.outime.app.presentation.screens.ClientHomeScreen
import com.outime.app.presentation.screens.CreateServiceScreen
import com.outime.app.presentation.screens.LoginScreen
import com.outime.app.presentation.screens.RegisterScreen
import com.outime.app.presentation.screens.SplashScreen
import com.outime.app.presentation.viewmodel.AuthViewModel
import com.outime.app.presentation.viewmodel.AuthViewModelFactory
import com.outime.app.presentation.viewmodel.ServiceViewModel
import com.outime.app.presentation.viewmodel.ServiceViewModelFactory

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()

    val authRepository = AuthRepositoryImpl(
        firebaseAuth = FirebaseAuth.getInstance(),
        firestore = FirebaseFirestore.getInstance()
    )

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository)
    )

    val serviceRepository = ServiceRepositoryImpl(
        firestore = FirebaseFirestore.getInstance()
    )

    val serviceViewModel: ServiceViewModel = viewModel(
        factory = ServiceViewModelFactory(serviceRepository)
    )

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {

        composable(Routes.SPLASH) {
            SplashScreen(
                authViewModel = authViewModel,

                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) {
                            inclusive = true
                        }
                    }
                },

                onNavigateToClientHome = {
                    navController.navigate(Routes.CLIENT_HOME) {
                        popUpTo(Routes.SPLASH) {
                            inclusive = true
                        }
                    }
                },

                onNavigateToBusinessHome = {
                    navController.navigate(Routes.BUSINESS_HOME) {
                        popUpTo(Routes.SPLASH) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.SPLASH) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel
            )
        }

        composable(Routes.CLIENT_HOME) {
            ClientHomeScreen()
        }

        composable(Routes.BUSINESS_HOME) {
            BusinessHomeScreen(
                authViewModel = authViewModel,
                serviceViewModel = serviceViewModel,
                onNavigateToCreateService = {
                    navController.navigate(Routes.CREATE_SERVICE)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(Routes.CREATE_SERVICE) {
            val businessId = authViewModel.currentUserId() ?: ""
            CreateServiceScreen(
                serviceViewModel = serviceViewModel,
                businessId = businessId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
