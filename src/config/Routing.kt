package config

import fund_transfer.positions.PositionController
import fund_transfer.transfer.TransferController
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.patch
import io.ktor.routing.post
import org.koin.ktor.ext.get as inject

fun Routing.get(path: String, func: suspend (ApplicationCall) -> Unit) = get(path) { func(call) }
fun Routing.post(path: String, func: suspend (ApplicationCall) -> Unit) = post(path) { func(call) }
fun Routing.patch(path: String, func: suspend (ApplicationCall) -> Unit) = patch(path) { func(call) }
fun Routing.delete(path: String, func: suspend (ApplicationCall) -> Unit) = delete(path) { func(call) }

fun Routing.route() {

    val positionController = inject<PositionController>()
    val transferController = inject<TransferController>()

    get   ("/api/positions/{id}", positionController::getPosition)
    get   ("/api/positions", positionController::getAllPositions)
    post  ("/api/positions", positionController::createPosition)
    patch ("/api/positions/block/{id}", positionController::blockProfile)
    patch ("/api/positions/unblock/{id}", positionController::unblockProfile)

    post  ("/api/transfer", transferController::makeTransfer)
    get   ("/api/transfer/{id}", transferController::getTransfer)
}