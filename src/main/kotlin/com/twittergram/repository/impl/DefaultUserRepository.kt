package com.twittergram.repository.impl

import com.twittergram.model.User
import com.twittergram.model.UserSignature
import com.twittergram.repository.UserRepository
import kotlin.random.Random
import kotlin.math.abs
import java.time.Instant

class DefaultUserRepository: UserRepository {

    private val users: MutableMap<String, User> = mutableMapOf()

    override fun create(username: String, password: String, name: String): User {
        /* It's OK to have not unique id - username is unique. */
        var id = abs(Random.nextLong())
        val sign = UserSignature(id, username)
        val createdUser = User(sign, password, name, Instant.now().toString())
        users.put(username, createdUser)
        return createdUser

    }

    override fun get(username: String): User? {
        return users.get(username)
    }

    override fun delete(username: String): Boolean {
        return users.remove(username) != null
    }
}