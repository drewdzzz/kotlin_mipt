package com.twittergram.repository.impl

import com.twittergram.model.Post
import com.twittergram.repository.PostRepository
import java.time.Instant
import kotlin.random.Random

class DefaultPostRepository: PostRepository {

    private val posts: MutableMap<Long, Post> = mutableMapOf()

    override fun getAll(): Collection<Post> {
        return posts.values.toList()
    }

    override fun getPage(start: Long, size: Long): Collection<Post> {
        return posts.toSortedMap().tailMap(start).values.toList()
            .take(size.toInt())
    }

    override fun getById(id: Long): Post? {
        return posts.get(id)
    }

    override fun create(postText: String): Post {
        var id = Random.nextLong()
        /* Make sure that id is positive and unique. */
        while (id <= 0 || posts.contains(id)) {
            id = Random.nextLong() 
        }
        val createdPost = Post(
            id,
            postText,
            Instant.now().toString(),
        )
        posts.put(id, createdPost)
        return createdPost
    }

    override fun update(id: Long, newPostText: String): Post? {
        return posts.get(id)?.update(
            newPostText,
            Instant.now().toString()
        )
    }

    override fun delete(id: Long): Boolean {
        return posts.remove(id) != null
    }
}