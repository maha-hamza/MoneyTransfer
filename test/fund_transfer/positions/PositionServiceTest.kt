package fund_transfer.positions

import abstracts.createPosition
import abstracts.AbstractDBTest
import config.lazyInject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.KoinComponent
import java.time.Instant

class PositionServiceTest : AbstractDBTest(), KoinComponent {

    private val positionService by lazyInject<PositionService>()

    @Nested
    inner class PositionCreation {

        @Test
        fun `Should create position`() {
            val newPosition = NewPosition(
                portfolioId = "port-id",
                balance = 100.0,
                positionType = "Money Account",
                assetType = AssetType.EGP
            )
            val position = positionService.createPosition(newPosition)

            assertThat(position).isEqualToIgnoringGivenFields(
                Position(
                    portfolioId = "port-id",
                    balance = 100.0,
                    positionType = "Money Account",
                    assetType = AssetType.EGP,
                    id = "ignored",
                    dateOpened = Instant.ofEpochMilli(0),
                    dateClosed = null,
                    comments = "",
                    locked = false,
                    blocked = false
                ),
                Position::id.name,
                Position::dateOpened.name,
                Position::dateClosed.name,
                Position::comments.name
            )
        }
    }

    @Nested
    inner class PositionRetrieval {

        @Test
        fun `Should get position by id`() {
            val position = createPosition(id = "any-id", balance = 2000.0)

            val result = positionService.getPosition("any-id")
            assertThat(result).isEqualTo(position)
        }

        @Test
        fun `Shouldn't get position by id (not-found)`() {
            val result = positionService.getPosition("any-id")
            assertThat(result).isNull()
        }

        @Test
        fun `Should return all positions while flags are false`() {
            val position1 = createPosition(balance = 1000.0)
            val position2 = createPosition(balance = 2000.0)
            val position3 = createPosition(balance = 3000.0)

            val result = positionService.getPositions(locked = false, blocked = false)
            assertThat(result).containsExactlyInAnyOrder(position1, position2, position3)
        }

        @Test
        fun `Should return locked positions`() {
            val position1 = createPosition(balance = 1000.0, locked = true)
            createPosition(balance = 2000.0)
            val position3 = createPosition(balance = 3000.0, locked = true)

            val result = positionService.getPositions(locked = true, blocked = false)
            assertThat(result).containsExactlyInAnyOrder(position1, position3)
        }

        @Test
        fun `Should return blocked positions`() {
            createPosition(balance = 1000.0)
            val position2 = createPosition(balance = 2000.0, blocked = true)
            createPosition(balance = 3000.0)

            val result = positionService.getPositions(locked = false, blocked = true)
            assertThat(result).containsExactlyInAnyOrder(position2)
        }

        @Test
        fun `Should return empty list is no positions existed`() {
            val result = positionService.getPositions(locked = false, blocked = false)
            assertThat(result).isEmpty()
        }

        @Test
        fun `Should return empty list if no match found`() {
            createPosition(balance = 1000.0)
            createPosition(balance = 2000.0)
            createPosition(balance = 3000.0)

            val result = positionService.getPositions(locked = true, blocked = true)
            assertThat(result).isEmpty()
        }
    }

    @Nested
    inner class PositionUpdate {

        @Test
        fun `Should update Balance Correctly`() {
            val position = createPosition(balance = 2000.0)

            val result = positionService.updateBalance(id = position.id, newBalance = position.balance + 1000)
            assertThat(result.balance).isEqualTo(3000.0)
        }

        @Test
        fun `Should unlock position`() {
            val position = createPosition(locked = true)

            val result = positionService.unlockPosition(id = position.id)
            assertThat(result.locked).isFalse()
        }

        @Test
        fun `Should lock position`() {
            val position = createPosition()

            val result = positionService.lockPosition(id = position.id)
            assertThat(result.locked).isTrue()
        }

        @Test
        fun `Should block position`() {
            val position = createPosition()

            val result = positionService.blockPosition(id = position.id)
            assertThat(result.blocked).isTrue()
        }

        @Test
        fun `Should fail blocking position (not-found)`() {
            assertThrows<PositionNotFoundException> {
                positionService.blockPosition(id = "any-id")
            }
        }

        @Test
        fun `Should unblock position`() {
            val position = createPosition()

            val result = positionService.unblockPosition(id = position.id)
            assertThat(result.blocked).isFalse()
        }

        @Test
        fun `Should fail unblocking position (not-found)`() {
            assertThrows<PositionNotFoundException> {
                positionService.unblockPosition(id = "any-id")
            }
        }

    }

}