package com.twittergram

import com.twittergram.repository.PostRepository
import com.twittergram.repository.impl.DefaultPostRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val postModule = module {
    singleOf(::DefaultPostRepository) bind PostRepository::class
}