package fund_transfer.transfer

import config.body
import config.respondNullable
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.koin.core.KoinComponent
import org.koin.core.inject

class TransferController : KoinComponent {

    private val transferService by inject<TransferService>()

    @Synchronized
    suspend fun makeTransfer(call: ApplicationCall) {
        val transfer = call.body<NewTransfer>()
        call.respond(
            HttpStatusCode.Created,
            transferService.makeTransfer(transfer)
        )
    }

    suspend fun getTransfer(call: ApplicationCall) {
        val id = call.parameters["id"]!!
        call.respondNullable(transferService.getTransfer(id))
    }

}