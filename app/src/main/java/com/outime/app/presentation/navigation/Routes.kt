package com.outime.app.presentation.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"

    const val CLIENT_HOME = "client_home"
    const val BUSINESS_HOME = "business_home"

    const val CREATE_SERVICE = "create_service"
    const val CREATE_BUSINESS = "create_business"
    const val SCHEDULE_MANAGEMENT = "schedule_management"
    const val BUSINESS_APPOINTMENTS = "business_appointments"
    const val CLIENT_APPOINTMENTS = "client_appointments"

    const val BUSINESS_SERVICES = "business_services"
    const val BUSINESS_PROFILE = "business_profile"
    const val CLIENT_PROFILE = "client_profile"

    const val CLIENT_PROFILE_EDIT = "client_profile_edit"
    const val BUSINESS_PROFILE_EDIT = "business_profile_edit"

    const val FORGOT_PASSWORD = "forgot_password"

    const val BUSINESS_DETAIL = "business_detail/{businessId}"

    fun businessDetail(businessId: String) = "business_detail/$businessId"

    const val BOOKING = "booking/{businessId}/{serviceId}?businessName={businessName}&serviceName={serviceName}&clientId={clientId}&durationMinutes={durationMinutes}"

    fun booking(
        businessId: String,
        serviceId: String,
        businessName: String,
        serviceName: String,
        clientId: String,
        durationMinutes: Int
    ) = "booking/$businessId/$serviceId?businessName=${businessName.encode()}&serviceName=${serviceName.encode()}&clientId=$clientId&durationMinutes=$durationMinutes"

    private fun String.encode(): String =
        java.net.URLEncoder.encode(this, "UTF-8")
}