package com.yusuf.expensepro.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.yusuf.expensepro.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = auth.currentUser != null

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: Flow<UserProfile?> = _userProfile.asStateFlow()

    init { auth.currentUser?.uid?.let { fetchProfile(it) } }

    private fun fetchProfile(uid: String) {
        firestore.collection("users").document(uid).addSnapshotListener { snap, _ ->
            snap?.let {
                _userProfile.value = UserProfile(
                    uid       = it.getString("uid") ?: uid,
                    fullName  = it.getString("fullName") ?: "",
                    email     = it.getString("email") ?: "",
                    phoneNumber = it.getString("phoneNumber") ?: "",
                    photoUrl  = it.getString("photoUrl") ?: "",
                    createdAt = it.getLong("createdAt") ?: 0L
                )
            }
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.uid?.let { fetchProfile(it) }
            AuthResult.Success
        } catch (e: Exception) { AuthResult.Error(friendlyError(e)) }
    }

    suspend fun register(email: String, password: String, name: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return AuthResult.Error("Registration failed")
            result.user?.updateProfile(userProfileChangeRequest { displayName = name })?.await()
            val profile = UserProfile(uid = uid, fullName = name, email = email, createdAt = System.currentTimeMillis() / 1000)
            firestore.collection("users").document(uid).set(profile.toMap()).await()
            fetchProfile(uid)
            AuthResult.Success
        } catch (e: Exception) { AuthResult.Error(friendlyError(e)) }
    }

    suspend fun signInWithGoogle(account: GoogleSignInAccount): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val uid = result.user?.uid ?: return AuthResult.Error("Google sign-in failed")
            if (result.additionalUserInfo?.isNewUser == true) {
                val profile = UserProfile(uid = uid, fullName = account.displayName ?: "",
                    email = account.email ?: "", photoUrl = account.photoUrl?.toString() ?: "",
                    createdAt = System.currentTimeMillis() / 1000)
                firestore.collection("users").document(uid).set(profile.toMap()).await()
            }
            fetchProfile(uid)
            AuthResult.Success
        } catch (e: Exception) { AuthResult.Error(friendlyError(e)) }
    }

    suspend fun sendPasswordReset(email: String): AuthResult {
        return try { auth.sendPasswordResetEmail(email).await(); AuthResult.Success }
        catch (e: Exception) { AuthResult.Error(friendlyError(e)) }
    }

    suspend fun updateProfile(name: String, phone: String): AuthResult {
        val uid = auth.currentUser?.uid ?: return AuthResult.Error("Not logged in")
        return try {
            auth.currentUser?.updateProfile(userProfileChangeRequest { displayName = name })?.await()
            firestore.collection("users").document(uid).update(mapOf("fullName" to name, "phoneNumber" to phone)).await()
            AuthResult.Success
        } catch (e: Exception) { AuthResult.Error(e.message ?: "Update failed") }
    }

    fun logout() { auth.signOut(); _userProfile.value = null }

    private fun UserProfile.toMap() = mapOf(
        "uid" to uid, "fullName" to fullName, "email" to email,
        "phoneNumber" to phoneNumber, "photoUrl" to photoUrl, "createdAt" to createdAt
    )

    private fun friendlyError(e: Exception): String = when (e) {
        is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
        is FirebaseAuthUserCollisionException      -> "An account with this email already exists"
        is FirebaseAuthWeakPasswordException       -> "Password is too weak (min 6 characters)"
        is FirebaseAuthInvalidUserException        -> "No account found with this email"
        else -> e.message ?: "Something went wrong"
    }
}
