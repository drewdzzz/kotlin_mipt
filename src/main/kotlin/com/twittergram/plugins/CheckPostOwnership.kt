package com.twittergram.plugins

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

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun CheckPostOwnership(postRepository: PostRepository) =
    createRouteScopedPlugin(name = "CheckPostOwnership") {
        on(AuthenticationChecked) { call ->
            val postId = call.parameters["id"]?.toLong() ?: 0
            val principal = call.principal<JWTPrincipal>()

            if (principal != null) {
                val username = principal.payload.getClaim("username").asString()
                val uid = principal.payload.getClaim("uid").asLong()
                val sign = UserSignature(uid, username)

                val post = postRepository.getById(postId)
                if (post == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else if (post.author != sign)
                    call.respond(HttpStatusCode.Forbidden)
            } else {
                    call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
