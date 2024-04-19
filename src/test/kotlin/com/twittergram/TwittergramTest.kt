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
import com.twittergram.model.Post

class TwittergramTest {

    @Serializable
    data class Token (
        val token: String,
    )

    @Test
    fun testBasic() = testApplication {
        application {
            configureServer()
            authApi()
            postApi()
        }

        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        var response = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("testBasic", "1111", "Name"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest("testBasic", "1111"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val token = Json.decodeFromString(Token.serializer(), response.bodyAsText()).token

        response = client.put("/posts/create") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(CreatePostRequest("Hello, twittergram!"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val created = Json.decodeFromString(Post.serializer(), response.bodyAsText())
        assertEquals("Hello, twittergram!", created.text)
        assertNotEquals(0, created.id)

        response = client.get("/posts/${created.id}/get") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val got_created = Json.decodeFromString(Post.serializer(), response.bodyAsText())
        assertEquals(created.text, got_created.text)
        assertEquals(created.id, got_created.id)

        response = client.patch("/posts/${created.id}/update") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(UpdatePostRequest("Bye, twittergram!"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val updated = Json.decodeFromString(Post.serializer(), response.bodyAsText())
        assertEquals("Bye, twittergram!", updated.text)
        assertEquals(created.id, updated.id)

        response = client.get("/posts/${created.id}/get") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val got_updated = Json.decodeFromString(Post.serializer(), response.bodyAsText())
        assertEquals(updated.text, got_updated.text)
        assertEquals(updated.id, got_updated.id)

        response = client.delete("/posts/${created.id}/delete") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, response.status)

        /* Check if the post was actually deleted. */
        response = client.get("/posts/${created.id}/get") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testEmpty() = testApplication {
        application {
            configureServer()
            authApi()
            postApi()
        }

        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        var response = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("testEmpty", "1111", "Name"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest("testEmpty", "1111"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val token = Json.decodeFromString(Token.serializer(), response.bodyAsText()).token

        response = client.get("/posts/all") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("[\n]", response.bodyAsText())

        response = client.get("/posts/42/get") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)

        response = client.patch("/posts/42/update") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(UpdatePostRequest("Invalid post"))
        }
        assertEquals(HttpStatusCode.NotFound, response.status)

        response = client.delete("/posts/42/delete") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testFullscanAndPagination() = testApplication {
        application {
            configureServer()
            authApi()
            postApi()
        }

        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        var response = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("testRanges", "1111", "Name"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest("testRanges", "1111"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val token = Json.decodeFromString(Token.serializer(), response.bodyAsText()).token

        for (i in 1..10) {
            client.put("/posts/create") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(CreatePostRequest("Post $i"))
            }
        }

        /* Check if fullscan fetches all posts */
        val fullscan = client.get("/posts/all") {
            bearerAuth(token)
        }
        val allPosts = Json.decodeFromString<Array<Post>>(fullscan.bodyAsText())
        assertEquals(10, allPosts.size)

        /* Pagination */
        var start: Long = 0
        for (page_num in 1..3) {
            response = client.get("/posts/page") {
                bearerAuth(token)
                parameter("start", start)
                parameter("size", 3)
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val page = Json.decodeFromString<Array<Post>>(response.bodyAsText())
            assertEquals(3, page.size)
            assertTrue(page[0].id < page[1].id)
            assertTrue(page[1].id < page[2].id)
            /* Advance cursor */
            start = page[2].id + 1
        }
        response = client.get("/posts/page") {
            bearerAuth(token)
            parameter("start", start)
            parameter("size", 3)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val page = Json.decodeFromString<Array<Post>>(response.bodyAsText())
        assertEquals(1, page.size)
    }

    @Test
    fun testNoAccessWithoutLogin() = testApplication {
        application {
            configureServer()
            authApi()
            postApi()
        }

        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        /* Login and create the post for the test. */
        var response = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("UserTestNoAccess", "1111", "Name"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest("UserTestNoAccess", "1111"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val token = Json.decodeFromString(Token.serializer(), response.bodyAsText()).token

        response = client.put("/posts/create") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(CreatePostRequest("Hello, twittergram!"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val created = Json.decodeFromString(Post.serializer(), response.bodyAsText())
        assertEquals("Hello, twittergram!", created.text)
        val postId = created.id
        assertNotEquals(0, postId)

        response = client.put("/posts/create") {
            contentType(ContentType.Application.Json)
            setBody(CreatePostRequest("Hello, twittergram!"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)

        response = client.get("/posts/${postId}/get") {
            contentType(ContentType.Application.Json)
            setBody(UpdatePostRequest("Bye, twittergram!"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)

        response = client.patch("/posts/${postId}/update") {
            contentType(ContentType.Application.Json)
            setBody(UpdatePostRequest("Bye, twittergram!"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testAccessToPostsOfOtherPeople() = testApplication {
        application {
            configureServer()
            authApi()
            postApi()
        }

        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        /* Login as PostOwner and create post. */
        var response = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("PostOwner", "1111", "Name"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest("PostOwner", "1111"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        var token = Json.decodeFromString(Token.serializer(), response.bodyAsText()).token

        response = client.put("/posts/create") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(CreatePostRequest("Hello, twittergram!"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val created = Json.decodeFromString(Post.serializer(), response.bodyAsText())
        assertEquals("Hello, twittergram!", created.text)
        val postId = created.id
        assertNotEquals(0, postId)

        /* Login as PostVisitor and access to the created post. */
        response = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("PostVisitor", "1111", "Name"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest("PostVisitor", "1111"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        token = Json.decodeFromString(Token.serializer(), response.bodyAsText()).token

        response = client.get("/posts/${postId}/get") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val got_created = Json.decodeFromString(Post.serializer(), response.bodyAsText())
        assertEquals(created.text, got_created.text)
        assertEquals(created.id, got_created.id)

        response = client.patch("/posts/${postId}/update") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(UpdatePostRequest("Bye, twittergram!"))
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)

        response = client.delete("/posts/${created.id}/delete") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    } 
}
