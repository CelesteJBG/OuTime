package com.outime.app.presentation.navigation

import com.outime.app.domain.model.UserRole
import com.outime.app.presentation.viewmodel.AuthViewModel
import com.outime.app.presentation.viewmodel.BusinessViewModel

/**
 * Determina la pantalla de destino tras el login/registro y también durante el
 * arranque en frío (Splash). Lógica compartida para no duplicarla.
 *
 * Reutiliza el estado del negocio ya cargado en [BusinessViewModel].
 */
suspend fun resolvePostLoginDestination(
    authViewModel: AuthViewModel,
    businessViewModel: BusinessViewModel
): String {
    val user = authViewModel.getCurrentUser() ?: return Routes.LOGIN
    return when (user.role) {
        UserRole.CLIENT -> Routes.CLIENT_HOME
        UserRole.BUSINESS -> {
            val ownerId = authViewModel.currentUserId() ?: ""
            val business = businessViewModel.getBusinessByOwnerId(ownerId)
            if (business != null) Routes.BUSINESS_HOME else Routes.CREATE_BUSINESS
        }
    }
}