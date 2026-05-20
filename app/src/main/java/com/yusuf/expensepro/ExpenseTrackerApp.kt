package com.yusuf.expensepro

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.yusuf.expensepro.data.sync.FirestoreSyncService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ExpenseTrackerApp : Application() {

    @Inject lateinit var syncService: FirestoreSyncService
    @Inject lateinit var firebaseAuth: FirebaseAuth

    private val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        firebaseAuth.addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                appScope.launch {
                    // Pull remote data first (new device / fresh install)
                    syncService.pullAll()
                    // Then push any local changes made while offline
                    syncService.syncAll()
                }
            }
        }
    }
}
