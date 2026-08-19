package com.outime.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.outime.app.data.repository.AppointmentRepositoryImpl
import com.outime.app.data.repository.AuthRepositoryImpl
import com.outime.app.data.repository.BusinessRepositoryImpl
import com.outime.app.data.repository.ScheduleRepositoryImpl
import com.outime.app.data.repository.ServiceRepositoryImpl
import com.outime.app.presentation.components.OuTimeBottomNavigation
import com.outime.app.presentation.screens.BookingScreen
import com.outime.app.presentation.screens.BusinessAppointmentsScreen
import com.outime.app.presentation.screens.BusinessDetailScreen
import com.outime.app.presentation.screens.BusinessHomeScreen
import com.outime.app.presentation.screens.BusinessProfileScreen
import com.outime.app.presentation.screens.BusinessServicesScreen
import com.outime.app.presentation.screens.BusinessStatisticsScreen
import com.outime.app.presentation.screens.ClientAppointmentsScreen
import com.outime.app.presentation.screens.ClientHomeScreen
import com.outime.app.presentation.screens.ClientProfileScreen
import com.outime.app.presentation.screens.CreateBusinessScreen
import com.outime.app.presentation.screens.CreateServiceScreen
import com.outime.app.presentation.screens.EditBusinessProfileScreen
import com.outime.app.presentation.screens.EditClientProfileScreen
import com.outime.app.presentation.screens.EditServiceScreen
import com.outime.app.presentation.screens.LoginScreen
import com.outime.app.presentation.screens.RegisterScreen
import com.outime.app.presentation.screens.ScheduleManagementScreen
import com.outime.app.presentation.screens.SplashScreen
import com.outime.app.presentation.screens.ForgotPasswordScreen
import com.outime.app.presentation.viewmodel.AppointmentViewModel
import com.outime.app.presentation.viewmodel.AppointmentViewModelFactory
import com.outime.app.presentation.viewmodel.AuthViewModel
import com.outime.app.presentation.viewmodel.AuthViewModelFactory
import com.outime.app.presentation.viewmodel.BusinessCatalogViewModel
import com.outime.app.presentation.viewmodel.BusinessCatalogViewModelFactory
import com.outime.app.presentation.viewmodel.BusinessViewModel
import com.outime.app.presentation.viewmodel.BusinessViewModelFactory
import com.outime.app.presentation.viewmodel.ScheduleViewModel
import com.outime.app.presentation.viewmodel.ScheduleViewModelFactory
import com.outime.app.presentation.viewmodel.ServiceViewModel
import com.outime.app.presentation.viewmodel.ServiceViewModelFactory
import com.outime.app.presentation.viewmodel.StatisticsViewModel
import com.outime.app.presentation.viewmodel.StatisticsViewModelFactory

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

    val businessRepository = BusinessRepositoryImpl(
        firestore = FirebaseFirestore.getInstance()
    )

    val businessViewModel: BusinessViewModel = viewModel(
        factory = BusinessViewModelFactory(businessRepository, serviceRepository)
    )

    val businessCatalogViewModel: BusinessCatalogViewModel = viewModel(
        factory = BusinessCatalogViewModelFactory(businessRepository, serviceRepository)
    )

    val appointmentRepository = AppointmentRepositoryImpl(
        firestore = FirebaseFirestore.getInstance()
    )

    val appointmentViewModel: AppointmentViewModel = viewModel(
        factory = AppointmentViewModelFactory(appointmentRepository, authRepository)
    )

    val scheduleRepository = ScheduleRepositoryImpl(
        firestore = FirebaseFirestore.getInstance()
    )

    val scheduleViewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModelFactory(scheduleRepository)
    )

    val statisticsViewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModelFactory(appointmentRepository, serviceRepository)
    )

    // ── Track current route for Bottom Navigation ──────────────────
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Determine if bottom bar should be visible and which items to show
    val isBusinessMain = currentRoute in businessMainRoutes
    val isClientMain = currentRoute in clientMainRoutes
    val showBottomBar = isBusinessMain || isClientMain

    val navItems = when {
        isBusinessMain -> businessNavItems()
        isClientMain -> clientNavItems()
        else -> emptyList()
    }

    // Home route for tab navigation (popUpTo target)
    val homeRoute = when {
        isBusinessMain -> Routes.BUSINESS_HOME
        isClientMain -> Routes.CLIENT_HOME
        else -> Routes.SPLASH
    }

    // ── Scaffold with Bottom Navigation ───────────────────────────
    Scaffold(
        bottomBar = {
            if (showBottomBar && navItems.isNotEmpty()) {
                OuTimeBottomNavigation(
                    items = navItems,
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(homeRoute) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {

            composable(Routes.SPLASH) {
                SplashScreen(
                    authViewModel = authViewModel,
                    businessViewModel = businessViewModel,

                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },

                    onNavigateToClientHome = {
                        navController.navigate(Routes.CLIENT_HOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },

                    onNavigateToBusinessHome = {
                        navController.navigate(Routes.BUSINESS_HOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },

                    onNavigateToCreateBusiness = {
                        navController.navigate(Routes.CREATE_BUSINESS) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
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
                    },
                    onNavigateToRegister = {
                        navController.navigate(Routes.REGISTER)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Routes.FORGOT_PASSWORD)
                    }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onBack = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate(Routes.SPLASH) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    authViewModel = authViewModel,
                    onBack = {
                        navController.popBackStack()
                    },
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.FORGOT_PASSWORD) { inclusive = true }
                        }
                    }
                )
            }

            // ── CLIENT main routes ───────────────────────────────────

            composable(Routes.CLIENT_HOME) {
                ClientHomeScreen(
                    businessCatalogViewModel = businessCatalogViewModel,
                    authViewModel = authViewModel,
                    onNavigateToDetail = { businessId ->
                        navController.navigate(Routes.businessDetail(businessId))
                    }
                )
            }

            composable(Routes.CLIENT_APPOINTMENTS) {
                val clientId = authViewModel.currentUserId() ?: ""
                ClientAppointmentsScreen(
                    clientId = clientId,
                    appointmentViewModel = appointmentViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.CLIENT_PROFILE) {
                ClientProfileScreen(
                    authViewModel = authViewModel,
                    appointmentViewModel = appointmentViewModel,
                    onNavigateToAppointments = {
                        // Desde Perfil: push simple para volver a Perfil con Atrás.
                        navController.navigate(Routes.CLIENT_APPOINTMENTS)
                    },
                    onNavigateToEdit = {
                        navController.navigate(Routes.CLIENT_PROFILE_EDIT)
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0)
                        }
                    }
                )
            }

            // ── BUSINESS main routes ────────────────────────────────

            composable(Routes.BUSINESS_HOME) {

                BusinessHomeScreen(
                    serviceViewModel = serviceViewModel,
                    businessViewModel = businessViewModel,
                    appointmentViewModel = appointmentViewModel,
                    authViewModel = authViewModel,
                    onNavigateToCreateService = {
                        navController.navigate(Routes.CREATE_SERVICE)
                    },
                    onNavigateToBusinessAppointments = {
                        navController.navigate(Routes.BUSINESS_APPOINTMENTS) {
                            popUpTo(Routes.BUSINESS_HOME) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToBusinessServices = {
                        navController.navigate(Routes.BUSINESS_SERVICES) {
                            popUpTo(Routes.BUSINESS_HOME) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0)
                        }
                    }
                )
            }

            composable(Routes.BUSINESS_APPOINTMENTS) {
                val businessId = businessViewModel.currentBusinessId() ?: ""
                BusinessAppointmentsScreen(
                    businessId = businessId,
                    appointmentViewModel = appointmentViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.BUSINESS_SERVICES) {
                val businessId = businessViewModel.currentBusinessId() ?: ""
                BusinessServicesScreen(
                    businessId = businessId,
                    serviceViewModel = serviceViewModel,
                    onNavigateToCreateService = {
                        navController.navigate(Routes.CREATE_SERVICE)
                    },
                    onEditService = { serviceId ->
                        navController.navigate(Routes.editService(serviceId))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.SCHEDULE_MANAGEMENT) {
                val businessId = businessViewModel.currentBusinessId() ?: ""
                ScheduleManagementScreen(
                    businessId = businessId,
                    scheduleViewModel = scheduleViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.BUSINESS_PROFILE) {
                BusinessProfileScreen(
                    authViewModel = authViewModel,
                    businessViewModel = businessViewModel,
                    serviceViewModel = serviceViewModel,
                    onNavigateToServices = {
                        // Desde Perfil: push simple para volver a Perfil con Atrás.
                        navController.navigate(Routes.BUSINESS_SERVICES)
                    },
                    onNavigateToSchedule = {
                        // Desde Perfil: push simple para volver a Perfil con Atrás.
                        navController.navigate(Routes.SCHEDULE_MANAGEMENT)
                    },
                    onNavigateToEdit = {
                        navController.navigate(Routes.BUSINESS_PROFILE_EDIT)
                    },
                    onNavigateToStatistics = {
                        navController.navigate(Routes.BUSINESS_STATISTICS)
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0)
                        }
                    }
                )
            }

            composable(Routes.BUSINESS_STATISTICS) {
                val businessId = businessViewModel.currentBusinessId() ?: ""
                BusinessStatisticsScreen(
                    businessId = businessId,
                    statisticsViewModel = statisticsViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // ── Detail / Booking routes (no bottom bar) ─────────────

            composable(
                route = Routes.BUSINESS_DETAIL,
                arguments = listOf(
                    navArgument("businessId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
                BusinessDetailScreen(
                    businessId = businessId,
                    businessCatalogViewModel = businessCatalogViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onReserveClick = { serviceId, serviceName, durationMinutes, servicePrice ->
                        val business = businessCatalogViewModel.uiState.value.selectedBusiness
                        val clientId = authViewModel.currentUserId() ?: ""
                        if (business != null && clientId.isNotBlank()) {
                            navController.navigate(
                                Routes.booking(
                                    businessId = businessId,
                                    serviceId = serviceId,
                                    businessName = business.name,
                                    serviceName = serviceName,
                                    clientId = clientId,
                                    durationMinutes = durationMinutes,
                                    servicePrice = servicePrice
                                )
                            )
                        }
                    }
                )
            }

            composable(
                route = Routes.BOOKING,
                arguments = listOf(
                    navArgument("businessId") { type = NavType.StringType },
                    navArgument("serviceId") { type = NavType.StringType },
                    navArgument("businessName") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("serviceName") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("clientId") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("durationMinutes") {
                        type = NavType.IntType
                        defaultValue = 30
                    },
                    navArgument("servicePrice") {
                        type = NavType.FloatType
                        defaultValue = 0f
                    }
                )
            ) { backStackEntry ->
                val args = backStackEntry.arguments
                val businessId = args?.getString("businessId") ?: ""
                val serviceId = args?.getString("serviceId") ?: ""
                val businessName = java.net.URLDecoder.decode(
                    args?.getString("businessName") ?: "", "UTF-8"
                )
                val serviceName = java.net.URLDecoder.decode(
                    args?.getString("serviceName") ?: "", "UTF-8"
                )
                val clientId = args?.getString("clientId") ?: ""
                val durationMinutes = args?.getInt("durationMinutes") ?: 30
                val servicePrice = (args?.getFloat("servicePrice") ?: 0f).toDouble()

                BookingScreen(
                    clientId = clientId,
                    businessId = businessId,
                    businessName = businessName,
                    serviceId = serviceId,
                    serviceName = serviceName,
                    durationMinutes = durationMinutes,
                    servicePrice = servicePrice,
                    appointmentViewModel = appointmentViewModel,
                    scheduleViewModel = scheduleViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // ── Edit profile routes (no bottom bar) ─────────────────
            composable(Routes.CLIENT_PROFILE_EDIT) {
                EditClientProfileScreen(
                    authViewModel = authViewModel,
                    onSaved = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.BUSINESS_PROFILE_EDIT) {
                // Garantiza que el negocio esté cargado antes de editar
                LaunchedEffect(Unit) {
                    val ownerId = authViewModel.currentUserId() ?: return@LaunchedEffect
                    if (businessViewModel.uiState.value.business == null) {
                        businessViewModel.loadBusinessByOwnerId(ownerId)
                    }
                }
                EditBusinessProfileScreen(
                    businessViewModel = businessViewModel,
                    authViewModel = authViewModel,
                    onSaved = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // ── Create routes (no bottom bar) ────────────────────────

            composable(Routes.CREATE_BUSINESS) {
                val ownerId = authViewModel.currentUserId() ?: ""
                CreateBusinessScreen(
                    businessViewModel = businessViewModel,
                    ownerId = ownerId,
                    onBusinessCreated = {
                        navController.navigate(Routes.BUSINESS_HOME) {
                            popUpTo(Routes.CREATE_BUSINESS) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.CREATE_SERVICE) {
                val businessId = businessViewModel.currentBusinessId() ?: ""
                CreateServiceScreen(
                    serviceViewModel = serviceViewModel,
                    businessId = businessId,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Routes.EDIT_SERVICE,
                arguments = listOf(
                    navArgument("serviceId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
                val businessId = businessViewModel.currentBusinessId() ?: ""
                EditServiceScreen(
                    serviceId = serviceId,
                    businessId = businessId,
                    serviceViewModel = serviceViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}