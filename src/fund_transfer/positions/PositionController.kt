package fund_transfer.positions

import config.body
import config.respondNullable
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.koin.core.KoinComponent
import org.koin.core.inject

class PositionController : KoinComponent {

    private val positionService by inject<PositionService>()

    suspend fun getPosition(call: ApplicationCall) {
        val id = call.parameters["id"]!!
        call.respondNullable(positionService.getPosition(id = id))
    }

    suspend fun createPosition(call: ApplicationCall) {
        val position = call.body<NewPosition>()
        call.respond(
            HttpStatusCode.Created,
            positionService.createPosition(position = position)
        )
    }

    suspend fun getAllPositions(call: ApplicationCall) {
        val locked = call.parameters["locked"] == "true"
        val blocked = call.parameters["blocked"] == "true"

        call.respond(
            positionService.getPositions(
                locked = locked,
                blocked = blocked
            )
        )
    }

    suspend fun blockProfile(call: ApplicationCall) {
        val id = call.parameters["id"]!!
        call.respond(positionService.blockPosition(id))
    }

    suspend fun unblockProfile(call: ApplicationCall) {
        val id = call.parameters["id"]!!
        call.respond(positionService.unblockPosition(id))
    }
}