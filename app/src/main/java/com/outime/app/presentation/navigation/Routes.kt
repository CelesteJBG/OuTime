package com.outime.app.presentation.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"

    const val CLIENT_HOME = "client_home"
    const val BUSINESS_HOME = "business_home"

    const val CREATE_SERVICE = "create_service"
    const val CREATE_BUSINESS = "create_business"

    const val BUSINESS_DETAIL = "business_detail/{businessId}"

    fun businessDetail(businessId: String) = "business_detail/$businessId"

    const val BOOKING = "booking/{businessId}/{serviceId}?businessName={businessName}&serviceName={serviceName}&clientId={clientId}"

    fun booking(
        businessId: String,
        serviceId: String,
        businessName: String,
        serviceName: String,
        clientId: String
    ) = "booking/$businessId/$serviceId?businessName=${businessName.encode()}&serviceName=${serviceName.encode()}&clientId=$clientId"

    private fun String.encode(): String =
        java.net.URLEncoder.encode(this, "UTF-8")
}
