package com.yusuf.expensepro.di

/**
 * ─────────────────────────────────────────────────────────────
 *  UPDATED AppModule.kt  — replace the original with this
 * ─────────────────────────────────────────────────────────────
 *
 *  Changes over the original:
 *  1. SessionManager provided (DataStore session persistence)
 *  2. Use cases bound explicitly (optional — Hilt can inject
 *     them directly since they're @Inject constructor classes,
 *     but explicit binding is clearer for newcomers)
 *  3. NetworkModule scaffold commented out for Phase 2 Retrofit
 *
 *  File location: app/src/main/java/com/yusuf/expensepro/di/AppModule.kt
 */

import android.content.Context
import androidx.room.Room
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.yusuf.expensepro.data.local.ExpenseDatabase
import com.yusuf.expensepro.data.local.dao.*
import com.yusuf.expensepro.data.repository.*
import com.yusuf.expensepro.utils.SessionManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance().also {
            it.firestoreSettings = firestoreSettings { isPersistenceEnabled = true }
        }

    @Provides @Singleton
    fun provideGoogleSignInClient(@ApplicationContext ctx: Context): GoogleSignInClient =
        GoogleSignIn.getClient(
            ctx,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // ⚠️  Replace with your actual Firebase Console Web Client ID
                .requestIdToken("YOUR_WEB_CLIENT_ID.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build()
        )

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): ExpenseDatabase =
        Room.databaseBuilder(ctx, ExpenseDatabase::class.java, "expense_db")
            .fallbackToDestructiveMigration()
            .build()

    // ── DAOs ──────────────────────────────────────────────────────────────────

    @Provides fun provideTransactionDao(db: ExpenseDatabase): TransactionDao  = db.transactionDao()
    @Provides fun provideBudgetDao(db: ExpenseDatabase): BudgetDao            = db.budgetDao()
    @Provides fun provideSplitGroupDao(db: ExpenseDatabase): SplitGroupDao    = db.splitGroupDao()
    @Provides fun provideSplitMemberDao(db: ExpenseDatabase): SplitMemberDao  = db.splitMemberDao()
    @Provides fun provideSplitExpenseDao(db: ExpenseDatabase): SplitExpenseDao= db.splitExpenseDao()
    @Provides fun provideSplitShareDao(db: ExpenseDatabase): SplitShareDao    = db.splitShareDao()
    @Provides fun provideSettlementDao(db: ExpenseDatabase): SettlementDao    = db.settlementDao()

    // ── Session ───────────────────────────────────────────────────────────────

    @Provides @Singleton
    fun provideSessionManager(@ApplicationContext ctx: Context): SessionManager =
        SessionManager(ctx)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds @Singleton
    abstract fun bindSplitRepository(impl: SplitRepositoryImpl): SplitRepository
}

// ─────────────────────────────────────────────────────────────────────────────
//  FUTURE — Phase 2: Spring Boot + Retrofit
// ─────────────────────────────────────────────────────────────────────────────

/*
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.expensepro.com/"

    @Provides @Singleton
    fun provideOkHttpClient(sessionManager: SessionManager): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))   // JWT token injection
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                        else HttpLoggingInterceptor.Level.NONE
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides @Singleton
    fun provideTransactionApiService(retrofit: Retrofit): TransactionApiService =
        retrofit.create(TransactionApiService::class.java)

    @Provides @Singleton
    fun provideSplitApiService(retrofit: Retrofit): SplitApiService =
        retrofit.create(SplitApiService::class.java)
}

// JWT interceptor — attaches Bearer token to every request
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sessionManager.getJwtToken() }
        val request = chain.request().newBuilder()
            .apply { token?.let { addHeader("Authorization", "Bearer $it") } }
            .build()
        return chain.proceed(request)
    }
}
*/
