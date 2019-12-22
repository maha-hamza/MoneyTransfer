package fund_transfer.transfer

import abstracts.createPosition
import abstracts.createTransfer
import abstracts.AbstractDBTest
import config.lazyInject
import fund_transfer.positions.*
import org.assertj.core.api.Assertions.*
import org.joda.time.DateTime
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.KoinComponent
import java.time.Instant

class TransferServiceTest : AbstractDBTest(), KoinComponent {

    private val transferService by lazyInject<TransferService>()
    private val positionService by lazyInject<PositionService>()

    @Nested
    inner class TransferProcessing {

        @Test
        fun `Fail transfer processing (Sender Not Found)`() {
            val to = createPosition()

            val transfer = NewTransfer(
                fromPosition = "from-any",
                toPosition = to.id,
                amount = 100.0
            )
            assertThrows<PositionNotFoundException> {
                transferService.makeTransfer(transfer)
            }
        }

        @Test
        fun `Fail transfer processing (Sender position is blocked)`() {
            val from = createPosition(blocked = true)
            val to = createPosition()

            val transfer = NewTransfer(
                fromPosition = from.id,
                toPosition = to.id,
                amount = 100.0
            )
            assertThrows<BlockedAccountException> {
                transferService.makeTransfer(transfer)
            }

        }

        @Test
        fun `Fail transfer processing (Sender position is under processing)`() {
            val from = createPosition(locked = true)
            val to = createPosition()

            val transfer = NewTransfer(
                fromPosition = from.id,
                toPosition = to.id,
                amount = 100.0
            )
            assertThrows<LockedAccountException> {
                transferService.makeTransfer(transfer)
            }

        }

        @Test
        fun `Fail transfer processing (insufficient balance)`() {
            val from = createPosition(balance = 50.0)
            val to = createPosition()

            val transfer = NewTransfer(
                fromPosition = from.id,
                toPosition = to.id,
                amount = 100.0
            )
            assertThrows<InsufficientBalanceException> {
                transferService.makeTransfer(transfer)
            }

        }

        @Test
        fun `Fail transfer processing (Sender position is closed)`() {
            val from = createPosition(balance = 100.0, dateClosed = DateTime.now())
            val to = createPosition()

            val transfer = NewTransfer(
                fromPosition = from.id,
                toPosition = to.id,
                amount = 100.0
            )
            assertThrows<ClosedAccountException> {
                transferService.makeTransfer(transfer)
            }

        }

        @Test
        fun `Fail transfer processing (Receiver position is closed)`() {
            val from = createPosition(balance = 100.0)
            val to = createPosition(dateClosed = DateTime.now())

            val transfer = NewTransfer(
                fromPosition = from.id,
                toPosition = to.id,
                amount = 100.0
            )
            assertThrows<ClosedAccountException> {
                transferService.makeTransfer(transfer)
            }

        }

        @Test
        fun `Fail transfer processing (negative transfer amount)`() {
            val from = createPosition(balance = 1000.0)
            val to = createPosition()

            val transfer = NewTransfer(
                fromPosition = from.id,
                toPosition = to.id,
                amount = -100.0
            )
            assertThrows<NegativeTransferAmountException> {
                transferService.makeTransfer(transfer)
            }

        }

        @Test
        fun `Fail transfer processing (same position transfer)`() {
            val position = createPosition(balance = 1000.0)

            val transfer = NewTransfer(
                fromPosition = position.id,
                toPosition = position.id,
                amount = 100.0
            )
            assertThrows<SamePositionTransferException> {
                transferService.makeTransfer(transfer)
            }

        }

        @Test
        fun `Fail transfer processing (receiver doesn't exist)`() {
            val from = createPosition(balance = 1000.0)

            val transfer = NewTransfer(
                fromPosition = from.id,
                toPosition = "to",
                amount = 100.0
            )
            assertThrows<PositionNotFoundException> {
                transferService.makeTransfer(transfer)
            }

        }

        @Test
        fun `Fail transfer processing (receiver position is blocked)`() {
            val from = createPosition(balance = 1000.0)
            val to = createPosition(blocked = true)

            val transfer = NewTransfer(
                fromPosition = from.id,
                toPosition = to.id,
                amount = 100.0
            )
            assertThrows<BlockedAccountException> {
                transferService.makeTransfer(transfer)
            }

        }

        @Test
        fun `Should transfer successfully`() {
            val from = createPosition(balance = 1000.0)
            val to = createPosition()

            val newT = NewTransfer(
                fromPosition = from.id,
                toPosition = to.id,
                amount = 100.0
            )

            val transfer = transferService.makeTransfer(newT)

            assertThat(transfer).isEqualToIgnoringGivenFields(
                Transfer(
                    id = "",
                    fromPosition = from.id,
                    toPosition = to.id,
                    initiatedAt = Instant.ofEpochMilli(0),
                    finishedAt = Instant.ofEpochMilli(0),
                    amount = 100.0,
                    status = TransferStatus.ACCEPTED,
                    comments = null
                ),
                Transfer::id.name,
                Transfer::initiatedAt.name,
                Transfer::finishedAt.name
            )

            val sender = positionService.getPosition(from.id)
            val receiver = positionService.getPosition(to.id)
            assertThat(sender?.balance).isEqualTo(900.0)
            assertThat(receiver?.balance).isEqualTo(100.0)
        }
    }

    @Nested
    inner class TransferRetrieval {

        @Test
        fun `Should retrieve transfer`() {
            val id = createTransfer()
            val transfer = transferService.getTransfer(id)
            assertThat(transfer).isNotNull
        }

        @Test
        fun `Can't retrieve transfer (not-found)`() {
            val transfer = transferService.getTransfer("any-id")
            assertThat(transfer).isNull()
        }
    }

}