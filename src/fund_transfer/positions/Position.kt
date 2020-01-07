package fund_transfer.positions

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import java.math.BigDecimal
import java.time.Instant

object Positions : Table() {
    val id = varchar(name = "id", length = 50).primaryKey()
    val portfolioId = varchar(name = "portfolio_id", length = 50)
    val dateOpened = datetime("date_opened")
    val dateClosed = datetime("date_closed").nullable()
    val balance = decimal("balance", 20, 2).nullable()
    val blocked = bool("blocked")
    val locked = bool("locked")
    val positionType = varchar("position_type", 20)
    val assetType = enumerationByName("asset_type", 10, AssetType::class)
    val comments = varchar("comments", 255)
}

data class Position(
    val id: String,
    val portfolioId: String,
    val dateOpened: Instant,
    val dateClosed: Instant?,
    val balance: BigDecimal = BigDecimal.ZERO,
    val blocked: Boolean = false,
    val locked: Boolean = false,
    val positionType: String?,
    val assetType: AssetType,
    val comments: String?
)

data class NewPosition(
    val portfolioId: String,
    val balance: BigDecimal,
    val positionType: String?,
    val assetType: AssetType,
    val comments: String? = null
)

fun toPosition(row: ResultRow): Position =
    Position(
        id = row[Positions.id],
        portfolioId = row[Positions.portfolioId],
        dateOpened = row[Positions.dateOpened].let { Instant.ofEpochMilli(it.millis) },
        dateClosed = row[Positions.dateClosed]?.let { Instant.ofEpochMilli(it.millis) },
        balance = row[Positions.balance] ?: BigDecimal.ZERO,
        blocked = row[Positions.blocked],
        locked = row[Positions.locked],
        positionType = row[Positions.positionType],
        assetType = row[Positions.assetType],
        comments = row[Positions.comments]
    )