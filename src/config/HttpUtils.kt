package config

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import fund_transfer.positions.BodyDeserializationException
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond

suspend fun ApplicationCall.respondNullable(msg: Any?) {
    when (msg) {
        null -> respond(HttpStatusCode.NotFound, "")
        else -> respond(msg)
    }
}

suspend inline fun <reified T : Any> ApplicationCall.body(): T {
    return try {
        receive()
    } catch (e: MissingKotlinParameterException) {
        throw BodyDeserializationException(e.message ?: "Missing Kotlin Parameter")
    } catch (e: MismatchedInputException) {
        throw BodyDeserializationException(e.message ?: "Miss Matched Kotlin Parameter")
    }
}