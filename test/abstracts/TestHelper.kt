package abstracts

import fund_transfer.positions.AssetType
import fund_transfer.positions.Position
import fund_transfer.positions.Positions
import fund_transfer.positions.toPosition
import fund_transfer.transfer.TransferStatus
import fund_transfer.transfer.Transfers
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.time.Instant
import java.util.*

fun createPosition(
    id: String = "REV${(1 until 100).random()}-${UUID.randomUUID()}-00",
    portfolioId: String = "any_p_id",
    assetType: AssetType = AssetType.EUR,
    balance: Double = 0.0,
    comments: String = "",
    blocked: Boolean = false,
    locked: Boolean = false,
    dateOpened: DateTime = DateTime(Instant.now().toEpochMilli()),
    dateClosed: DateTime? = null
): Position {
    return transaction {
        val id = Positions.insert {
            it[Positions.id] = id
            it[Positions.portfolioId] = portfolioId
            it[Positions.dateOpened] = dateOpened
            it[Positions.dateClosed] = dateClosed
            it[positionType] = "Money Account"
            it[Positions.assetType] = assetType
            it[Positions.balance] = balance
            it[Positions.comments] = comments
            it[Positions.locked] = locked
            it[Positions.blocked] = blocked
        } get Positions.id
        Positions
            .selectAll()
            .andWhere { Positions.id eq id }
            .map { toPosition(it) }
            .first()
    }
}

fun createTransfer(): String {
    return transaction {
        Transfers.insert {
            it[id] = "TRN${(1 until 100).random()}-${UUID.randomUUID()}"
            it[fromPosition] = createPosition().id
            it[toPosition] = createPosition().id
            it[initiatedAt] = DateTime(Instant.now().toEpochMilli())
            it[finishedAt] = DateTime(Instant.now().toEpochMilli())
            it[status] = TransferStatus.ACCEPTED
        } get Transfers.id
    }
}