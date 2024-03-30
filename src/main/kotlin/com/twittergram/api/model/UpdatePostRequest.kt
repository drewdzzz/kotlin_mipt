package com.twittergram.api.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePostRequest(
    val newPostText: String
)