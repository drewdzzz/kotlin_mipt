package com.twittergram.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val sign: UserSignature,
    val password: String,
    val name: String,
    val creationTime: String
)