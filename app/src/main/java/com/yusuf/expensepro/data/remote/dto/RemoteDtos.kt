package com.yusuf.expensepro.data.remote.dto

/**
 * ─────────────────────────────────────────────────────────────
 *  DATA TRANSFER OBJECTS (DTOs)
 * ─────────────────────────────────────────────────────────────
 *
 *  DTOs represent the shape of data sent/received over the
 *  network. They are deliberately SEPARATE from domain models.
 *
 *  Why separate?
 *  - API response format ≠ domain model (snake_case vs camelCase,
 *    extra fields, different nullability, etc.)
 *  - Domain models should never change due to API changes
 *  - Easy to add @SerializedName for Gson / @JsonProperty for Jackson
 *
 *  Mapper functions (toDto() / toDomain()) live at the bottom
 *  of each DTO class to keep the conversion logic close to the type.
 *
 *  These are commented out until Retrofit is added in Phase 2.
 *  The structure is already correct — just uncomment + add dependency.
 */

// import com.yusuf.expensepro.domain.model.*
// import com.google.gson.annotations.SerializedName
// import java.time.LocalDate

/*
// ── Auth DTOs ─────────────────────────────────────────────────────────────────

data class LoginRequestDto(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequestDto(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email")     val email: String,
    @SerializedName("password")  val password: String
)

data class AuthResponseDto(
    @SerializedName("access_token")  val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in")    val expiresIn: Long,
    @SerializedName("user")          val user: UserDto
)

data class RefreshTokenDto(
    @SerializedName("refresh_token") val refreshToken: String
)

data class UserDto(
    @SerializedName("id")           val id: String,
    @SerializedName("full_name")    val fullName: String,
    @SerializedName("email")        val email: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("photo_url")    val photoUrl: String?,
    @SerializedName("created_at")   val createdAt: Long
) {
    fun toDomain() = UserProfile(
        uid         = id,
        fullName    = fullName,
        email       = email,
        phoneNumber = phoneNumber ?: "",
        photoUrl    = photoUrl ?: "",
        createdAt   = createdAt
    )
}

// ── Transaction DTOs ──────────────────────────────────────────────────────────

data class TransactionDto(
    @SerializedName("id")       val id: Long,
    @SerializedName("title")    val title: String,
    @SerializedName("amount")   val amount: Double,
    @SerializedName("type")     val type: String,       // "INCOME" | "EXPENSE"
    @SerializedName("category") val category: String,
    @SerializedName("date")     val date: String,       // ISO "2025-01-15"
    @SerializedName("note")     val note: String?
) {
    fun toDomain() = Transaction(
        id       = id,
        title    = title,
        amount   = amount,
        type     = TransactionType.valueOf(type),
        category = Category.valueOf(category),
        date     = LocalDate.parse(date),
        note     = note ?: ""
    )
}

data class CreateTransactionDto(
    @SerializedName("title")    val title: String,
    @SerializedName("amount")   val amount: Double,
    @SerializedName("type")     val type: String,
    @SerializedName("category") val category: String,
    @SerializedName("date")     val date: String,
    @SerializedName("note")     val note: String = ""
)

fun Transaction.toCreateDto() = CreateTransactionDto(
    title    = title,
    amount   = amount,
    type     = type.name,
    category = category.name,
    date     = date.toString(),
    note     = note
)

// ── Split Group DTOs ──────────────────────────────────────────────────────────

data class SplitGroupDto(
    @SerializedName("id")         val id: Long,
    @SerializedName("name")       val name: String,
    @SerializedName("icon")       val icon: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("members")    val members: List<SplitMemberDto> = emptyList()
) {
    fun toDomain() = SplitGroup(
        id        = id,
        name      = name,
        icon      = icon,
        createdAt = LocalDate.parse(createdAt)
    )
}

data class CreateGroupDto(
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String
)

data class SplitMemberDto(
    @SerializedName("id")              val id: Long,
    @SerializedName("group_id")        val groupId: Long,
    @SerializedName("name")            val name: String,
    @SerializedName("is_current_user") val isCurrentUser: Boolean
) {
    fun toDomain() = SplitMember(id, groupId, name, isCurrentUser)
}

data class SplitExpenseDto(
    @SerializedName("id")               val id: Long,
    @SerializedName("group_id")         val groupId: Long,
    @SerializedName("title")            val title: String,
    @SerializedName("total_amount")     val totalAmount: Double,
    @SerializedName("paid_by_member_id") val paidByMemberId: Long,
    @SerializedName("date")             val date: String,
    @SerializedName("note")             val note: String?,
    @SerializedName("category")         val category: String,
    @SerializedName("split_type")       val splitType: String,
    @SerializedName("shares")           val shares: List<SplitShareDto> = emptyList()
) {
    fun toDomain() = SplitExpense(
        id              = id,
        groupId         = groupId,
        title           = title,
        totalAmount     = totalAmount,
        paidByMemberId  = paidByMemberId,
        date            = LocalDate.parse(date),
        note            = note ?: "",
        category        = Category.valueOf(category),
        splitType       = SplitType.valueOf(splitType)
    )
}

data class CreateSplitExpenseDto(
    @SerializedName("title")             val title: String,
    @SerializedName("total_amount")      val totalAmount: Double,
    @SerializedName("paid_by_member_id") val paidByMemberId: Long,
    @SerializedName("category")          val category: String,
    @SerializedName("split_type")        val splitType: String,
    @SerializedName("note")              val note: String = "",
    @SerializedName("custom_shares")     val customShares: Map<Long, Double>? = null
)

data class SplitShareDto(
    @SerializedName("id")            val id: Long,
    @SerializedName("expense_id")    val expenseId: Long,
    @SerializedName("member_id")     val memberId: Long,
    @SerializedName("share_amount")  val shareAmount: Double,
    @SerializedName("is_settled")    val isSettled: Boolean
) {
    fun toDomain() = SplitShare(id, expenseId, memberId, shareAmount, isSettled)
}

data class SettlementDto(
    @SerializedName("id")               val id: Long,
    @SerializedName("group_id")         val groupId: Long,
    @SerializedName("from_member_id")   val fromMemberId: Long,
    @SerializedName("to_member_id")     val toMemberId: Long,
    @SerializedName("amount")           val amount: Double,
    @SerializedName("note")             val note: String?,
    @SerializedName("date")             val date: String,
    @SerializedName("is_partial")       val isPartial: Boolean
) {
    fun toDomain() = Settlement(id, groupId, fromMemberId, toMemberId, amount, note ?: "", LocalDate.parse(date), isPartial)
}

data class CreateSettlementDto(
    @SerializedName("from_member_id") val fromMemberId: Long,
    @SerializedName("to_member_id")   val toMemberId: Long,
    @SerializedName("amount")         val amount: Double,
    @SerializedName("note")           val note: String = "",
    @SerializedName("is_partial")     val isPartial: Boolean = false
)

data class MemberBalanceDto(
    @SerializedName("member_id")   val memberId: Long,
    @SerializedName("name")        val name: String,
    @SerializedName("total_paid")  val totalPaid: Double,
    @SerializedName("total_owed")  val totalOwed: Double,
    @SerializedName("net")         val net: Double
)
*/
