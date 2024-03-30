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

import com.twittergram.configureServer
import com.twittergram.api.postApi
import com.twittergram.api.model.*
import com.twittergram.model.Post

class TwittergramTest {
    @Test
    fun testBasic() = testApplication {
        application {
            configureServer()
            postApi()
        }

        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        var response = client.put("/posts/create") {
            contentType(ContentType.Application.Json)
            setBody(CreatePostRequest("Hello, twittergram!"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val created = Json.decodeFromString(Post.serializer(), response.bodyAsText())
        assertEquals("Hello, twittergram!", created.text)
        assertNotEquals(0, created.id)

        response = client.get("/posts/${created.id}/get")
        assertEquals(HttpStatusCode.OK, response.status)
        val got_created = Json.decodeFromString(Post.serializer(), response.bodyAsText())
        assertEquals(created.text, got_created.text)
        assertEquals(created.id, got_created.id)

        response = client.patch("/posts/${created.id}/update") {
            contentType(ContentType.Application.Json)
            setBody(UpdatePostRequest("Bye, twittergram!"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val updated = Json.decodeFromString(Post.serializer(), response.bodyAsText())
        assertEquals("Bye, twittergram!", updated.text)
        assertEquals(created.id, updated.id)

        response = client.get("/posts/${created.id}/get")
        assertEquals(HttpStatusCode.OK, response.status)
        val got_updated = Json.decodeFromString(Post.serializer(), response.bodyAsText())
        assertEquals(updated.text, got_updated.text)
        assertEquals(updated.id, got_updated.id)

        response = client.delete("/posts/${created.id}/delete")
        assertEquals(HttpStatusCode.OK, response.status)

        /* Check if the post was actually deleted. */
        response = client.get("/posts/${created.id}/get")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testEmpty() = testApplication {
        application {
            configureServer()
            postApi()
        }

        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        var response = client.get("/posts/all")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("[\n]", response.bodyAsText())

        response = client.get("/posts/42/get")
        assertEquals(HttpStatusCode.NotFound, response.status)

        response = client.patch("/posts/42/update") {
            contentType(ContentType.Application.Json)
            setBody(UpdatePostRequest("Invalid post"))
        }
        assertEquals(HttpStatusCode.NotFound, response.status)

        response = client.delete("/posts/42/delete")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testFullscanAndPagination() = testApplication {
        application {
            configureServer()
            postApi()
        }

        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        for (i in 1..10) {
            client.put("/posts/create") {
                contentType(ContentType.Application.Json)
                setBody(CreatePostRequest("Post $i"))
            }
        }

        /* Check if fullscan fetches all posts */
        val fullscan = client.get("/posts/all")
        val allPosts = Json.decodeFromString<Array<Post>>(fullscan.bodyAsText())
        assertEquals(10, allPosts.size)

        /* Pagination */
        var start: Long = 0
        for (page_num in 1..3) {
            val response = client.get("/posts/page") {
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
        val response = client.get("/posts/page") {
            parameter("start", start)
            parameter("size", 3)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val page = Json.decodeFromString<Array<Post>>(response.bodyAsText())
        assertEquals(1, page.size)
    }
}
