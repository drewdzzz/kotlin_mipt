package com.twittergram.api

import com.twittergram.api.model.AuthRequest
import com.twittergram.api.model.CreatePostRequest
import com.twittergram.api.model.UpdatePostRequest
import com.twittergram.model.User
import com.twittergram.model.UserSignature
import com.twittergram.repository.PostRepository
import com.twittergram.repository.UserRepository
import com.auth0.jwt.*
import com.auth0.jwt.algorithms.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import com.twittergram.plugins.CheckPostOwnership

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.postApi() {
    val secret = environment.config.property("jwt.secret").getString()
    val postRepository by inject<PostRepository>()
    val userRepository by inject<UserRepository>()

    install(Authentication) {
        jwt("post-auth-jwt") {
            verifier(JWT
                .require(Algorithm.HMAC256(secret))
                .build())
            validate { credential ->
                val username = credential.payload.getClaim("username").asString()
                val uid = credential.payload.getClaim("uid").asLong()
                val user = userRepository.get(username)
                if (uid != null && user?.sign?.id == uid)
                    JWTPrincipal(credential.payload)
                else
                    null
            }   
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized,
                             "Token is not valid")
            }
        } 
    }

    routing {
        authenticate("post-auth-jwt") {

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

                val principal = call.principal<JWTPrincipal>()
                if (principal != null) {
                    val username = principal.payload.getClaim("username").asString()
                    val uid = principal.payload.getClaim("uid").asLong()
                    val sign = UserSignature(uid, username)

                    val createdPost = postRepository.create(request.postText, sign)
                    call.respond(createdPost)
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }

            /* Route for posts that check ownership. */
            route("/posts/{id}/update") {
                install(CheckPostOwnership(postRepository))
                patch {
                    val postId = call.parameters["id"]?.toLong() ?: 0
                    val request = call.receive<UpdatePostRequest>()

                    val principal = call.principal<JWTPrincipal>()
                    val username = principal!!.payload.getClaim("username").asString()
                    val uid = principal.payload.getClaim("uid").asLong()
                    val sign = UserSignature(uid, username)

                    val oldPost = postRepository.getById(postId)
                    if (oldPost?.author != sign)
                        call.respond(HttpStatusCode.NotFound)

                    val post = postRepository.update(postId, request.newPostText)

                    if (post == null) {
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        call.respond(post)
                    }
                }
            }
            

            route("/posts/{id}/delete") {
                install(CheckPostOwnership(postRepository))
                delete {
                    val postId = call.parameters["id"]?.toLong() ?: 0

                    if (postRepository.delete(postId)) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }
    }
}
