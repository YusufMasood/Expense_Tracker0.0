package com.yusuf.expensepro.domain.model

import java.time.Instant

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val createdAt: Long = Instant.now().epochSecond
)
