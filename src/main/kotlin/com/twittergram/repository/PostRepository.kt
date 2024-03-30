package com.twittergram.repository

import com.twittergram.model.Post

interface PostRepository {

    fun getAll(): Collection<Post>

    /**
     * Returns page not greater than @a size starting from @a start.
     */
    fun getPage(start: Long, size: Long): Collection<Post>

    fun getById(id: Long): Post?

    fun create(postText: String): Post

    fun update(id: Long, newPostText: String): Post?

    fun delete(id: Long): Boolean
}