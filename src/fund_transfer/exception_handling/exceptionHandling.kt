package fund_transfer.exception_handling

import fund_transfer.positions.*
import fund_transfer.transfer.NegativeTransferAmountException
import fund_transfer.transfer.SamePositionTransferException
import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

val exceptionHandling: StatusPages.Configuration.() -> Unit = {

    exception<PositionNotFoundException> { cause ->
        call.respond(
            HttpStatusCode.NotFound,
            cause.message ?: "Position Not Found"
        )
    }

    exception<BlockedAccountException> { cause ->
        call.respond(
            HttpStatusCode.BadRequest,
            cause.message ?: "account is Blocked"
        )
    }

    exception<ClosedAccountException> { cause ->
        call.respond(
            HttpStatusCode.BadRequest,
            cause.message ?: "account is closed"
        )
    }

    exception<LockedAccountException> { cause ->
        call.respond(
            HttpStatusCode.BadRequest,
            cause.message ?: "Account is under processing in another transaction"
        )
    }

    exception<InsufficientBalanceException> { cause ->
        call.respond(
            HttpStatusCode.BadRequest,
            cause.message ?: "Insufficient balance in sender account"
        )
    }

    exception<SamePositionTransferException> { cause ->
        call.respond(
            HttpStatusCode.BadRequest,
            cause.message ?: "Can't Transfer money to the same account"
        )
    }

    exception<NegativeTransferAmountException> { cause ->
        call.respond(
            HttpStatusCode.BadRequest,
            cause.message ?: "Can't Transfer negative amount"
        )
    }

    exception<BodyDeserializationException> { cause ->
        call.respond(
            HttpStatusCode.BadRequest,
            cause.message!!
        )
    }
}