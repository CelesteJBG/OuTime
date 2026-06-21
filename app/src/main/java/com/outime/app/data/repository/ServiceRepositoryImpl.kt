package com.outime.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.outime.app.domain.model.Service
import com.outime.app.domain.repository.ServiceRepository
import kotlinx.coroutines.tasks.await

class ServiceRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ServiceRepository {

    companion object {
        private const val SERVICES_COLLECTION = "services"
    }

    override suspend fun createService(service: Service): Result<Unit> = try {
        val docRef = firestore
            .collection(SERVICES_COLLECTION)
            .document()

        val serviceWithId = service.copy(id = docRef.id)

        docRef.set(serviceWithId).await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getServicesByBusiness(businessId: String): Result<List<Service>> = try {
        val snapshot = firestore
            .collection(SERVICES_COLLECTION)
            .whereEqualTo("businessId", businessId)
            .get()
            .await()

        val services = snapshot.toObjects(Service::class.java)

        Result.success(services)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
