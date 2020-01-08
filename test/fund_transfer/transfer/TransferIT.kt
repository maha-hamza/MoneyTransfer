package fund_transfer.transfer

import abstracts.ProjectionAssertions.Companion.assertThat
import abstracts.createPosition
import abstracts.AbstractDBTest
import abstracts.JsonUtf8
import config.lazyInject
import fund_transfer.positions.Position
import fund_transfer.positions.PositionService
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class TransferIT : AbstractDBTest() {

    private val positionService by lazyInject<PositionService>()
    @Test
    fun `Should make transfer and ensure data is reflected in profiles`() {
        val from = createPosition(balance = BigDecimal(1000.0))
        val to = createPosition()

        handle(
            uri = "/api/transfer",
            method = HttpMethod.Post,
            body = NewTransfer(
                fromPosition = from.id,
                toPosition = to.id,
                amount = BigDecimal(100.0)
            )
        ) {
            assertThat(response)
                .status(HttpStatusCode.Created)
                .contentType(JsonUtf8)
                .body<Transfer>()
                .isEqualToIgnoringGivenFields(
                    Transfer(
                        fromPosition = from.id,
                        toPosition = to.id,
                        amount = BigDecimal.valueOf(10000, 2),
                        status = TransferStatus.ACCEPTED,
                        initiatedAt = Instant.now(),
                        finishedAt = Instant.now(),
                        comments = null,
                        id = ""
                    ),
                    Transfer::id.name,
                    Transfer::initiatedAt.name,
                    Transfer::finishedAt.name,
                    Transfer::comments.name
                )
        }

        val senderPosition = positionService.getPosition(from.id)
        val receiverPosition = positionService.getPosition(to.id)
        assertThat(senderPosition!!.balance).isEqualTo(BigDecimal.valueOf(90000, 2))
        assertThat(receiverPosition!!.balance).isEqualTo(BigDecimal.valueOf(10000, 2))

    }

    @Test
    fun `Should handle concurrent transfers without deadlock happening`() {
        runBlocking {
            val from = createPosition(balance = BigDecimal.valueOf(100000, 1))
            val to = createPosition(balance = BigDecimal.valueOf(100000, 1))

            val numOfTransactions = 11

            1.rangeTo(numOfTransactions).map {
                launch {
                    makeTransaction(from, to, BigDecimal.valueOf(1000, 1))
                    makeTransaction(to, from, BigDecimal.valueOf(100, 1))
                }
            }.forEach { job -> job.join() }


            assertThat(positionService.getPosition(from.id)?.balance)
                .isEqualTo(BigDecimal.valueOf(901000, 2))
            assertThat(positionService.getPosition(to.id)?.balance)
                .isEqualTo(BigDecimal.valueOf(1099000, 2))
        }
    }

    @Test
    fun `Should handle one direction multiple transfers successfully`() {
        runBlocking {
            val from = createPosition(balance = BigDecimal.TEN)
            val to = createPosition(balance = BigDecimal.TEN)

            1.rangeTo(5).map {
                launch {
                    makeTransaction(from, to, BigDecimal.ONE)
                }
            }.forEach { job -> job.join() }

            assertThat(positionService.getPosition(from.id)?.balance)
                .isEqualTo(BigDecimal.valueOf(500, 2))
            assertThat(positionService.getPosition(to.id)?.balance)
                .isEqualTo(BigDecimal.valueOf(1500, 2))
        }
    }

    private fun makeTransaction(from: Position, to: Position, amount: BigDecimal) {
        handle(
            uri = "/api/transfer",
            method = HttpMethod.Post,
            body = NewTransfer(
                fromPosition = from.id,
                toPosition = to.id,
                amount = amount
            )
        ) {
            assertThat(response)
                .status(HttpStatusCode.Created)
        }
    }
}