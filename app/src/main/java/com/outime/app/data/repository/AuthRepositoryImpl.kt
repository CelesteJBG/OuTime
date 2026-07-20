package com.outime.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.outime.app.domain.model.User
import com.outime.app.domain.model.UserRole
import com.outime.app.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<Unit> = try {
        val authResult = firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .await()

        val uid = authResult.user?.uid
            ?: return Result.failure(Exception("UID nulo tras el registro en Firebase Auth"))

        val user = User(
            id = uid,
            name = name,
            email = email,
            role = role,
            createdAt = System.currentTimeMillis()
        )

        firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .set(user)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<Unit> = try {
        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUserId(): String? =
        firebaseAuth.currentUser?.uid

    override suspend fun getCurrentUser(): User? {
        val uid = getCurrentUserId() ?: return null
        return try {
            val snapshot = firestore
                .collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            android.util.Log.d("OUTIME", "EXISTS = ${snapshot.exists()}")
            android.util.Log.d("OUTIME", "DATA = ${snapshot.data}")

            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
