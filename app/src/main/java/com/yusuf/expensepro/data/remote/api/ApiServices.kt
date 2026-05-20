package com.yusuf.expensepro.data.remote.api

/**
 * ─────────────────────────────────────────────────────────────
 *  FUTURE: Spring Boot REST API Integration
 * ─────────────────────────────────────────────────────────────
 *
 *  These Retrofit interfaces are scaffolded NOW so Phase 2
 *  (Spring Boot backend) can be plugged in without refactoring.
 *
 *  When you're ready:
 *  1. Add Retrofit dependency to build.gradle.kts
 *  2. Implement the interfaces in data/remote/datasource/
 *  3. Swap ExpenseRepositoryImpl to use RemoteExpenseDataSource
 *     OR add a SyncStrategy that merges local + remote
 *
 *  Future stack: Android → Retrofit → Spring Boot → PostgreSQL
 */

// Uncomment when adding Retrofit:
/*
import com.yusuf.expensepro.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

// ── Auth API ──────────────────────────────────────────────────────────────────

interface AuthApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenDto): Response<AuthResponseDto>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Unit>
}

// ── Transaction API ───────────────────────────────────────────────────────────

interface TransactionApiService {

    @GET("api/v1/transactions")
    suspend fun getAllTransactions(
        @Header("Authorization") token: String
    ): Response<List<TransactionDto>>

    @GET("api/v1/transactions/{id}")
    suspend fun getTransactionById(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<TransactionDto>

    @GET("api/v1/transactions/monthly")
    suspend fun getTransactionsByMonth(
        @Header("Authorization") token: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<List<TransactionDto>>

    @POST("api/v1/transactions")
    suspend fun createTransaction(
        @Header("Authorization") token: String,
        @Body transaction: CreateTransactionDto
    ): Response<TransactionDto>

    @PUT("api/v1/transactions/{id}")
    suspend fun updateTransaction(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body transaction: CreateTransactionDto
    ): Response<TransactionDto>

    @DELETE("api/v1/transactions/{id}")
    suspend fun deleteTransaction(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Unit>
}

// ── Split Group API ───────────────────────────────────────────────────────────

interface SplitApiService {

    @GET("api/v1/groups")
    suspend fun getAllGroups(
        @Header("Authorization") token: String
    ): Response<List<SplitGroupDto>>

    @POST("api/v1/groups")
    suspend fun createGroup(
        @Header("Authorization") token: String,
        @Body group: CreateGroupDto
    ): Response<SplitGroupDto>

    @DELETE("api/v1/groups/{id}")
    suspend fun deleteGroup(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Unit>

    @POST("api/v1/groups/{groupId}/expenses")
    suspend fun addExpense(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long,
        @Body expense: CreateSplitExpenseDto
    ): Response<SplitExpenseDto>

    @POST("api/v1/groups/{groupId}/settle")
    suspend fun settle(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long,
        @Body settlement: CreateSettlementDto
    ): Response<SettlementDto>

    @GET("api/v1/groups/{groupId}/balances")
    suspend fun getMemberBalances(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long
    ): Response<List<MemberBalanceDto>>
}
*/
