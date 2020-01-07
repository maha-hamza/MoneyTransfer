package fund_transfer.transfer

import abstracts.ProjectionAssertions.Companion.assertThat
import abstracts.createPosition
import abstracts.AbstractDBTest
import abstracts.JsonUtf8
import config.lazyInject
import fund_transfer.positions.PositionService
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import org.assertj.core.api.Assertions
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
                        amount = BigDecimal.valueOf(10000,2),
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
        Assertions.assertThat(senderPosition!!.balance).isEqualTo(BigDecimal.valueOf(90000,2))
        Assertions.assertThat(receiverPosition!!.balance).isEqualTo(BigDecimal.valueOf(10000,2))

    }
}