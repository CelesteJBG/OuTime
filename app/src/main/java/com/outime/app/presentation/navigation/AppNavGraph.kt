package com.outime.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.outime.app.data.repository.AppointmentRepositoryImpl
import com.outime.app.data.repository.AuthRepositoryImpl
import com.outime.app.data.repository.BusinessRepositoryImpl
import com.outime.app.data.repository.ScheduleRepositoryImpl
import com.outime.app.data.repository.ServiceRepositoryImpl
import com.outime.app.presentation.screens.BookingScreen
import com.outime.app.presentation.screens.BusinessAppointmentsScreen
import com.outime.app.presentation.screens.BusinessDetailScreen
import com.outime.app.presentation.screens.BusinessHomeScreen
import com.outime.app.presentation.screens.ClientAppointmentsScreen
import com.outime.app.presentation.screens.ClientHomeScreen
import com.outime.app.presentation.screens.CreateBusinessScreen
import com.outime.app.presentation.screens.CreateServiceScreen
import com.outime.app.presentation.screens.LoginScreen
import com.outime.app.presentation.screens.RegisterScreen
import com.outime.app.presentation.screens.ScheduleManagementScreen
import com.outime.app.presentation.screens.SplashScreen
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
        factory = AppointmentViewModelFactory(appointmentRepository)
    )

    val scheduleRepository = ScheduleRepositoryImpl(
        firestore = FirebaseFirestore.getInstance()
    )

    val scheduleViewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModelFactory(scheduleRepository)
    )

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
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

        composable(Routes.CLIENT_HOME) {
            ClientHomeScreen(
                businessCatalogViewModel = businessCatalogViewModel,
                authViewModel = authViewModel,
                onNavigateToDetail = { businessId ->
                    navController.navigate(Routes.businessDetail(businessId))
                },
                onNavigateToClientAppointments = {
                    navController.navigate(Routes.CLIENT_APPOINTMENTS)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0)
                    }
                }
            )
        }

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
                onReserveClick = { serviceId, serviceName, durationMinutes ->
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
                                durationMinutes = durationMinutes
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

            BookingScreen(
                clientId = clientId,
                businessId = businessId,
                businessName = businessName,
                serviceId = serviceId,
                serviceName = serviceName,
                durationMinutes = durationMinutes,
                appointmentViewModel = appointmentViewModel,
                scheduleViewModel = scheduleViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onBookingSuccess = {
                    navController.navigate(Routes.CLIENT_HOME) {
                        popUpTo(Routes.CLIENT_HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.BUSINESS_HOME) {

            BusinessHomeScreen(
                authViewModel = authViewModel,
                serviceViewModel = serviceViewModel,
                businessViewModel = businessViewModel,
                onNavigateToCreateService = {
                    navController.navigate(Routes.CREATE_SERVICE)
                },
                onNavigateToScheduleManagement = {
                    navController.navigate(Routes.SCHEDULE_MANAGEMENT)
                },
                onNavigateToBusinessAppointments = {
                    navController.navigate(Routes.BUSINESS_APPOINTMENTS)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0)
                    }
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
    }
}