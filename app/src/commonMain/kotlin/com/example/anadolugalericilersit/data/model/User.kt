package com.example.anadolugalericilersit.data.model

enum class Role {
    SUPER_ADMIN,
    ADMIN,
    MODERATOR,
    DEALER,
    USER
}

data class User(
    val uid: String = "",
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val role: Role = Role.USER,
    val dealerId: String? = null,
    val canIssueDamga: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
