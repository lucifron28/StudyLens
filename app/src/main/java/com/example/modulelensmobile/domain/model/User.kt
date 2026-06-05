package com.example.modulelensmobile.domain.model

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val profileImageUrl: String? = null
) {
    val fullName: String
        get() = "$firstName $lastName".trim().ifBlank {
            username.ifBlank { email }
        }
}
