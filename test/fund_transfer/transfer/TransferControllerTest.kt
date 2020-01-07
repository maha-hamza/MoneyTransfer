package fund_transfer.transfer

import abstracts.ProjectionAssertions.Companion.assertThat
import abstracts.createPosition
import abstracts.AbstractDBTest
import abstracts.JsonUtf8
import abstracts.PlainUtf8
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import org.joda.time.DateTime
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class TransferControllerTest : AbstractDBTest() {

    @Nested
    inner class TransferProcessing {
        @Test
        fun `Should make transfer`() {
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
        }

        @Test
        fun `Can't make transfer (sender-not-found)`() {
            val to = createPosition()

            handle(
                uri = "/api/transfer",
                method = HttpMethod.Post,
                body = NewTransfer(
                    fromPosition = "any",
                    toPosition = to.id,
                    amount = BigDecimal(100.0)
                )
            ) {
                assertThat(response)
                    .status(HttpStatusCode.NotFound)
                    .contentType(PlainUtf8)
                    .body<String>()
                    .isEqualTo("Sender Position Not found")
            }
        }

        @Test
        fun `Can't make transfer (sender is blocked)`() {
            val from = createPosition(blocked = true)
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
                    .status(HttpStatusCode.BadRequest)
                    .contentType(PlainUtf8)
                    .body<String>()
                    .isEqualTo("Sender Account is Blocked")
            }
        }

        @Test
        fun `Can't make transfer (sender is locked)`() {
            val from = createPosition(locked = true)
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
                    .status(HttpStatusCode.BadRequest)
                    .contentType(PlainUtf8)
                    .body<String>()
                    .isEqualTo("Account is under processing in another transaction")
            }
        }

        @Test
        fun `Can't make transfer (Insufficient balance)`() {
            val from = createPosition(balance = BigDecimal(50.0))
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
                    .status(HttpStatusCode.BadRequest)
                    .contentType(PlainUtf8)
                    .body<String>()
                    .isEqualTo("Insufficient balance in sender account")
            }
        }

        @Test
        fun `Can't make transfer (Sender Account closed)`() {
            val from = createPosition(
                dateClosed = DateTime.now(),
                balance = BigDecimal(1000.0)
            )
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
                    .status(HttpStatusCode.BadRequest)
                    .contentType(PlainUtf8)
                    .body<String>()
                    .isEqualTo("Sender account is closed")
            }
        }

        @Test
        fun `Can't make transfer (Negative value)`() {
            val from = createPosition(
                balance = BigDecimal(1000.0)
            )
            val to = createPosition()

            handle(
                uri = "/api/transfer",
                method = HttpMethod.Post,
                body = NewTransfer(
                    fromPosition = from.id,
                    toPosition = to.id,
                    amount = BigDecimal(-100.0)
                )
            ) {
                assertThat(response)
                    .status(HttpStatusCode.BadRequest)
                    .contentType(PlainUtf8)
                    .body<String>()
                    .isEqualTo("Can't Transfer negative amount")
            }
        }

        @Test
        fun `Can't make transfer (Same Account Transfer)`() {
            val position = createPosition(
                balance = BigDecimal(1000.0)
            )

            handle(
                uri = "/api/transfer",
                method = HttpMethod.Post,
                body = NewTransfer(
                    fromPosition = position.id,
                    toPosition = position.id,
                    amount = BigDecimal(100.0)
                )
            ) {
                assertThat(response)
                    .status(HttpStatusCode.BadRequest)
                    .contentType(PlainUtf8)
                    .body<String>()
                    .isEqualTo("Can't Transfer money to the same account")
            }
        }

        @Test
        fun `Can't make transfer (receiver not found)`() {
            val sender = createPosition(
                balance = BigDecimal(1000.0)
            )

            handle(
                uri = "/api/transfer",
                method = HttpMethod.Post,
                body = NewTransfer(
                    fromPosition = sender.id,
                    toPosition = "any-id",
                    amount = BigDecimal(100.0)
                )
            ) {
                assertThat(response)
                    .status(HttpStatusCode.NotFound)
                    .contentType(PlainUtf8)
                    .body<String>()
                    .isEqualTo("Receiver Position Not found")
            }
        }

        @Test
        fun `Can't make transfer (receiver is blocked)`() {
            val from = createPosition(
                balance = BigDecimal(1000.0)
            )

            val to = createPosition(blocked = true)

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
                    .status(HttpStatusCode.BadRequest)
                    .contentType(PlainUtf8)
                    .body<String>()
                    .isEqualTo("Receiver Account is Blocked")
            }
        }

        @Test
        fun `Can't make transfer (receiver is closed)`() {
            val from = createPosition(
                balance = BigDecimal(1000.0)
            )

            val to = createPosition(dateClosed = DateTime.now())

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
                    .status(HttpStatusCode.BadRequest)
                    .contentType(PlainUtf8)
                    .body<String>()
                    .isEqualTo("Receiver Account is closed")
            }
        }

    }
}