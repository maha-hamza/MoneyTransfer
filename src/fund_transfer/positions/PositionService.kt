package fund_transfer.positions

import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import java.math.BigDecimal
import java.sql.Connection
import java.time.Instant
import java.util.*

class PositionService : KoinComponent {

    fun getPosition(id: String): Position? {

        return transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
            Positions
                .selectAll()
                .andWhere { Positions.id eq id }
                .map { toPosition(it) }
                .firstOrNull()
        }
    }

    fun getPositions(
        locked: Boolean,
        blocked: Boolean
    ): List<Position> {
        return transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
            Positions
                .selectAll()
                .andWhere { Positions.locked eq locked }
                .andWhere { Positions.blocked eq blocked }
                .map { toPosition(it) }
        }
    }

    fun createPosition(
        position: NewPosition
    ): Position {
        return transaction {
            val id = Positions.insert {
                it[id] = "REV${(1 until 100).random()}-${UUID.randomUUID()}-00"
                it[portfolioId] = position.portfolioId
                it[dateOpened] = DateTime(Instant.now().toEpochMilli())
                it[positionType] = position.positionType ?: "Money Account"
                it[assetType] = position.assetType
                it[balance] = position.balance
                it[comments] = position.comments ?: ""

            } get Positions.id
            getPosition(id)!!
        }
    }

    fun blockPosition(
        id: String
    ): Position {
        getPosition(id)?.let {
            return transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
                Positions.update({ Positions.id eq id }) {
                    it[blocked] = true
                }
                getPosition(id)!!
            }
        } ?: throw PositionNotFoundException(null)
    }

    fun unblockPosition(
        id: String
    ): Position {
        getPosition(id)?.let {
            return transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
                Positions.update({ Positions.id eq id }) {
                    it[blocked] = false
                }
                getPosition(id)!!
            }
        } ?: throw PositionNotFoundException(null)
    }

    fun lockPosition(
        id: String
    ): Position {
        return transaction {
            Positions.update({ Positions.id eq id }) {
                it[locked] = true
            }
            getPosition(id)!!
        }
    }

    fun unlockPosition(
        id: String
    ): Position {
        return transaction {
            Positions.update({ Positions.id eq id }) {
                it[locked] = false
            }
            getPosition(id)!!
        }

    }

    //no -ve balance is set her because it's not called as standalone service ,
    // till now it's helper function
    //so validation is on the caller service
    fun updateBalance(
        id: String,
        newBalance: BigDecimal
    ): Position {
        return transaction {
            Positions.update({ Positions.id eq id }) {
                it[balance] = newBalance
            }
            getPosition(id)!!
        }
    }
}