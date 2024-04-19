package com.twittergram

import com.twittergram.repository.PostRepository
import com.twittergram.repository.impl.DefaultPostRepository
import com.twittergram.repository.UserRepository
import com.twittergram.repository.impl.DefaultUserRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val postModule = module {
    singleOf(::DefaultPostRepository) bind PostRepository::class
    singleOf(::DefaultUserRepository) bind UserRepository::class
}