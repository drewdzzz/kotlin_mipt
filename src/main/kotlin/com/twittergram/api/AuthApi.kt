package com.twittergram.api

import com.twittergram.api.model.AuthRequest
import com.twittergram.api.model.RegisterRequest
import com.twittergram.model.User
import com.twittergram.model.UserSignature
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

fun Application.authApi() {
    val secret = environment.config.property("jwt.secret").getString()
    val userRepository by inject<UserRepository>()

    routing {
        post("/register") {
            val user = call.receive<RegisterRequest>()
            if (user.username == "") {
                call.respond(HttpStatusCode.BadRequest, "Invalid username");
            } else if (user.password == "") {
                call.respond(HttpStatusCode.BadRequest, "Invalid password");
            } else if (user.name == "") {
                call.respond(HttpStatusCode.BadRequest, "Invalid name");
            } else if (userRepository.get(user.username) != null) {
                call.respond(HttpStatusCode.Conflict, "Username is occupied")
            } else {
                userRepository.create(user.username, user.password, user.name)
                call.respond(HttpStatusCode.OK)
            }
        }

        post("/login") {
            val req = call.receive<AuthRequest>()
            val storedUser = userRepository.get(req.username)
            if (storedUser?.password != req.password) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid password")
            } else {
                val token = JWT.create()
                    .withClaim("username", req.username)
                    .withClaim("uid", storedUser.sign.id)
                    .sign(Algorithm.HMAC256(secret))
                call.respond(hashMapOf("token" to token))
            }
        }
    }
}