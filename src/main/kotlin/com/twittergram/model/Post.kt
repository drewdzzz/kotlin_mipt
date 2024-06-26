package com.twittergram.model

import com.twittergram.model.UserSignature
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Long,
    var text: String,
    val createdAt: String,
    var modifiedAt: String,
    var author: UserSignature
) {
    constructor(id: Long, text: String, time: String, author: UserSignature):
        this(id, text, time, time, author) {}

    fun update(text: String, time: String): Post {
        this.text = text
        this.modifiedAt = time
        return this
    }
}