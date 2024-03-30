package com.twittergram.model

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Long,
    var text: String,
    val createdAt: String,
    var modifiedAt: String
) {
    constructor(id: Long, text: String, time: String): this(id, text, time, time) {}

    fun update(text: String, time: String): Post {
        this.text = text
        this.modifiedAt = time
        return this
    }
}