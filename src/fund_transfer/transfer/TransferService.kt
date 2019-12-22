package fund_transfer.transfer

import fund_transfer.positions.*
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.sql.Connection
import java.time.Instant
import java.util.*

class TransferService : KoinComponent {

    private val positionService by inject<PositionService>()

    fun makeTransfer(
        transfer: NewTransfer
    ): Transfer {
        return transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {

            val id = Transfers.insert {
                it[id] = "TRN${(1 until 100).random()}-${UUID.randomUUID()}"
                it[fromPosition] = transfer.fromPosition
                it[toPosition] = transfer.toPosition
                it[initiatedAt] = DateTime(Instant.now().toEpochMilli())
                it[status] = TransferStatus.INITIATED
            } get Transfers.id

            val from = positionService.getPosition(transfer.fromPosition)
            val to = positionService.getPosition(transfer.toPosition)
            try {
                validateData(from, to, transfer)
            } finally {
                Transfers.update({ Transfers.id eq id }) {
                    it[status] = TransferStatus.REJECTED
                }
            }
            positionService.lockPosition(transfer.fromPosition)
            
            positionService.updateBalance(id = from!!.id, newBalance = from.balance.minus(transfer.amount))
            positionService.updateBalance(id = to!!.id, newBalance = to.balance.plus(transfer.amount))

            Transfers.update({ Transfers.id eq id }) {
                it[finishedAt] = DateTime(Instant.now().toEpochMilli())
                it[status] = TransferStatus.ACCEPTED
                it[amount] = transfer.amount
            }

            positionService.unlockPosition(transfer.fromPosition)
            getTransfer(id)!!
        }
    }

    fun getTransfer(id: String)
            : Transfer? {
        return transaction {
            Transfers
                .selectAll()
                .andWhere { Transfers.id eq id }
                .map { toTransfer(it) }
                .firstOrNull()
        }
    }

    private fun validateData(from: Position?, to: Position?, transfer: NewTransfer) {
        from ?: throw PositionNotFoundException("Sender Position Not found")
        when {
            from.blocked -> throw BlockedAccountException("Sender Account is Blocked")
            from.locked -> throw LockedAccountException(null)
            from.balance < transfer.amount -> throw InsufficientBalanceException(null)
            from.dateClosed != null -> throw ClosedAccountException("Sender account is closed")
            transfer.amount < 0.0 -> throw NegativeTransferAmountException(null)
            transfer.fromPosition == transfer.toPosition -> throw SamePositionTransferException(null)
            else -> {
                to ?: throw PositionNotFoundException("Receiver Position Not found")
                if (to.blocked)
                    throw BlockedAccountException("Receiver Account is Blocked")
                if (to.dateClosed != null)
                    throw ClosedAccountException("Receiver Account is closed")
            }
        }
    }
}