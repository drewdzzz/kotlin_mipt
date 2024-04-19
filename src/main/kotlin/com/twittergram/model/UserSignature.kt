package com.twittergram.model

import kotlinx.serialization.Serializable

/**
 * Both id and username used for strong protection: when the user
 * will be deleted, a new user can occupy its username, but they are
 * most likely to have different ids, so in this case the new user
 * won't have access to the posts of the old one.
 */
@Serializable
data class UserSignature(
    val id: Long,
    val username: String
)