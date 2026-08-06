package com.outime.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.outime.app.domain.model.Business
import com.outime.app.domain.repository.BusinessRepository
import kotlinx.coroutines.tasks.await

class BusinessRepositoryImpl(
    private val firestore: FirebaseFirestore
) : BusinessRepository {

    companion object {
        private const val BUSINESSES_COLLECTION = "businesses"
    }

    override suspend fun createBusiness(business: Business): Result<Unit> = try {
        val docRef = firestore
            .collection(BUSINESSES_COLLECTION)
            .document()

        val businessWithId = business.copy(id = docRef.id)

        docRef.set(businessWithId).await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getBusinessByOwnerId(ownerId: String): Result<Business?> = try {
        val snapshot = firestore
            .collection(BUSINESSES_COLLECTION)
            .whereEqualTo("ownerId", ownerId)
            .get()
            .await()

        val business = snapshot.toObjects(Business::class.java).firstOrNull()

        Result.success(business)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getBusinessById(businessId: String): Result<Business?> = try {
        val snapshot = firestore
            .collection(BUSINESSES_COLLECTION)
            .document(businessId)
            .get()
            .await()

        val business = snapshot.toObject(Business::class.java)

        Result.success(business)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateBusiness(business: Business): Result<Unit> = try {
        firestore
            .collection(BUSINESSES_COLLECTION)
            .document(business.id)
            .set(business)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAllBusinesses(): Result<List<Business>> = try {
        val snapshot = firestore
            .collection(BUSINESSES_COLLECTION)
            .get()
            .await()

        val businesses = snapshot.toObjects(Business::class.java)

        Result.success(businesses)
    } catch (e: Exception) {
        Result.failure(e)
    }
}