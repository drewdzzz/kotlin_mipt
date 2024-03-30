package com.twittergram

import com.twittergram.api.postApi
import com.twittergram.postModule
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(Netty, port = 8080) {
        configureServer()
        postApi()
    }.start(wait = true)
}

fun Application.configureServer() {
    install(Koin) {
        modules(postModule)
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        })
    }
}