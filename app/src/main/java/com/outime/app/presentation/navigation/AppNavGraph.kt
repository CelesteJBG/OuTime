package com.outime.app.presentation.navigation
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.outime.app.data.repository.AuthRepositoryImpl
import com.outime.app.presentation.viewmodel.AuthViewModel
import com.outime.app.presentation.viewmodel.AuthViewModelFactory

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.outime.app.presentation.screens.LoginScreen
import com.outime.app.presentation.screens.RegisterScreen
import com.outime.app.presentation.screens.SplashScreen

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

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {

        composable(Routes.SPLASH) {
            SplashScreen()
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel
            )
        }
    }
}