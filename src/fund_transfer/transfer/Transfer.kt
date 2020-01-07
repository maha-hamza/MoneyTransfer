package fund_transfer.transfer

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import java.math.BigDecimal
import java.time.Instant

object Transfers : Table() {
    val id = varchar(name = "id", length = 50).primaryKey()
    val fromPosition = varchar("from_position", 50)
    val toPosition = varchar("to_position", 50)
    val initiatedAt = datetime("initiated_at")
    val finishedAt = datetime("finished_at").nullable()
    val amount = decimal("amount", 20, 2).nullable()
    val status = enumerationByName("status", 10, TransferStatus::class)
    val comments = varchar("comments", 255).nullable()
}

data class Transfer(
    val id: String,
    val fromPosition: String,
    val toPosition: String,
    val initiatedAt: Instant,
    val finishedAt: Instant,
    val amount: BigDecimal,
    val status: TransferStatus?,
    val comments: String?
)

data class NewTransfer(
    val fromPosition: String,
    val toPosition: String,
    val amount: BigDecimal
)

fun toTransfer(row: ResultRow): Transfer =
    Transfer(
        id = row[Transfers.id],
        fromPosition = row[Transfers.fromPosition],
        toPosition = row[Transfers.toPosition],
        initiatedAt = row[Transfers.initiatedAt].let { Instant.ofEpochMilli(it.millis) },
        finishedAt = row[Transfers.finishedAt]?.let { Instant.ofEpochMilli(it.millis) }!!,
        amount = row[Transfers.amount] ?: BigDecimal.ZERO,
        status = row[Transfers.status],
        comments = row[Transfers.comments]
    )