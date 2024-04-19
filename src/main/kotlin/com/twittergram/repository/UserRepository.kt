package com.twittergram.repository

import com.twittergram.model.User

interface UserRepository {

    fun create(username: String, password: String, name: String): User

    fun get(username: String): User?

    fun delete(username: String): Boolean
}