package com.twittergram.api.model

import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(
    val postText: String
)