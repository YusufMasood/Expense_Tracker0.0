package com.yusuf.expensepro.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SessionManager stores lightweight session state locally using DataStore.
 *
 * Why DataStore and not SharedPreferences?
 * - DataStore is coroutine-safe, no UI thread blocking
 * - Type-safe via Preferences.Key
 * - Better error handling with Flow + catch
 *
 * Why not rely only on Firebase Auth?
 * - App needs to know auth state BEFORE Firebase initialises
 * - Local state enables offline-first session checks
 * - When migrating to Spring Boot JWT: store token here
 *
 * Future Phase 2 additions:
 * - JWT_TOKEN key for Spring Boot auth
 * - TOKEN_EXPIRY for auto-refresh
 * - USER_ID for local data scoping
 */

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "expense_pro_session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val IS_LOGGED_IN    = booleanPreferencesKey("is_logged_in")
        private val USER_EMAIL      = stringPreferencesKey("user_email")
        private val USER_NAME       = stringPreferencesKey("user_name")
        private val USER_UID        = stringPreferencesKey("user_uid")
        private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

        // Future: JWT keys (Phase 2 - Spring Boot)
        // private val JWT_TOKEN      = stringPreferencesKey("jwt_token")
        // private val TOKEN_EXPIRY   = longPreferencesKey("token_expiry")
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[IS_LOGGED_IN] ?: false }

    val userEmail: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[USER_EMAIL] ?: "" }

    val userName: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[USER_NAME] ?: "" }

    val isOnboardingDone: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[ONBOARDING_DONE] ?: false }

    // ── Write ─────────────────────────────────────────────────────────────────

    suspend fun saveSession(uid: String, email: String, name: String) {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[USER_UID]     = uid
            prefs[USER_EMAIL]   = email
            prefs[USER_NAME]    = name
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = false
            prefs.remove(USER_UID)
            prefs.remove(USER_EMAIL)
            prefs.remove(USER_NAME)
            // Future: prefs.remove(JWT_TOKEN)
        }
    }

    suspend fun markOnboardingDone() {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_DONE] = true
        }
    }

    // Future Phase 2: JWT helpers
    // suspend fun saveJwtToken(token: String, expiryMillis: Long) { ... }
    // suspend fun getJwtToken(): String? { ... }
    // fun isTokenExpired(): Flow<Boolean> { ... }
}
