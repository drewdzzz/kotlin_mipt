package com.twittergram

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlin.test.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

import com.twittergram.configureServer
import com.twittergram.api.authApi
import com.twittergram.api.postApi
import com.twittergram.api.model.*

/** Tests that have nothing in common with posts, authorization only. */
class AuthTest {

    @Serializable
    data class Token (
        val token: String,
    )

    @Test
    fun uniqueUsername() = testApplication {
        application {
            configureServer()
            authApi()
        }

        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        var response = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("UniqueUsername", "1111", "Name"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        response = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("UniqueUsername", "2222", "Name"))
        }
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun invalidRegisterUserInput() = testApplication {
        application {
            configureServer()
            authApi()
        }

        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        var response = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("test", "1111", ""))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        response = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("test", "", "Name"))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        response = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("", "1111", "Name"))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
