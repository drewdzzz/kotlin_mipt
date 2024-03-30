package com.twittergram.api

import com.twittergram.api.model.CreatePostRequest
import com.twittergram.api.model.UpdatePostRequest
import com.twittergram.repository.PostRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.postApi() {
    routing {

        val postRepository by inject<PostRepository>()

        get("/posts/all") {
            val posts = postRepository.getAll()
            call.respond(posts)
        }

        get("/posts/{id}/get") {
            val postId = call.parameters["id"]?.toLong() ?: 0

            val post = postRepository.getById(postId)

            if (post == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(post)
            }
        }

        get("/posts/page") {
            val start = call.request.queryParameters["start"]?.toLong() ?: 0
            val size = call.request.queryParameters["size"]?.toLong() ?: 0

            val posts = postRepository.getPage(start, size)

            call.respond(posts)
        }

        put("/posts/create") {
            val request = call.receive<CreatePostRequest>()

            val createdPost = postRepository.create(request.postText)

            call.respond(createdPost)
        }

        patch("/posts/{id}/update") {
            val postId = call.parameters["id"]?.toLong() ?: 0
            val request = call.receive<UpdatePostRequest>()

            val post = postRepository.update(postId, request.newPostText)

            if (post == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(post)
            }
        }

        delete("/posts/{id}/delete") {
            val postId = call.parameters["id"]?.toLong() ?: 0

            if (postRepository.delete(postId)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}