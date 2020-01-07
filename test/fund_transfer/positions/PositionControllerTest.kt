package fund_transfer.positions

import abstracts.ProjectionAssertions.Companion.assertThat
import abstracts.createPosition
import abstracts.AbstractDBTest
import abstracts.JsonUtf8
import abstracts.PlainUtf8
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class PositionControllerTest : AbstractDBTest() {


    @Nested
    inner class PositionRetrieval {
        @Test
        fun `Should Retrieve Position by id`() {
            val expectedPosition = createPosition()

            handle("/api/positions/${expectedPosition.id}") {
                assertThat(response)
                    .status(HttpStatusCode.OK)
                    .contentType(JsonUtf8)
                    .body<Position>()
                    .isEqualTo(expectedPosition)
            }
        }

        @Test
        fun `Should return not found status if position doesn't exist`() {

            handle("/api/positions/any-id") {
                assertThat(response)
                    .status(HttpStatusCode.NotFound)
                    .contentType(PlainUtf8)
            }
        }

        @Test
        fun `Should Retrieve all Position (not blocked - not locked)`() {
            val expectedPosition1 = createPosition()
            val expectedPosition2 = createPosition()

            handle("/api/positions") {
                assertThat(response)
                    .status(HttpStatusCode.OK)
                    .contentType(JsonUtf8)
                    .listBody<Position>()
                    .isEqualTo(
                        listOf(
                            expectedPosition1,
                            expectedPosition2
                        )
                    )
            }
        }

        @Test
        fun `Should Retrieve empty list if no positions found`() {

            handle("/api/positions") {
                assertThat(response)
                    .status(HttpStatusCode.OK)
                    .contentType(JsonUtf8)
                    .listBody<Position>()
                    .isEmpty()
            }
        }

        @Test
        fun `Should Retrieve all Position (blocked - not locked)`() {
            val expectedPosition1 = createPosition(blocked = true)
            createPosition()
            val expectedPosition3 = createPosition(blocked = true)

            handle("/api/positions?blocked=true") {
                assertThat(response)
                    .status(HttpStatusCode.OK)
                    .contentType(JsonUtf8)
                    .listBody<Position>()
                    .isEqualTo(
                        listOf(
                            expectedPosition1,
                            expectedPosition3
                        )
                    )
            }
        }

        @Test
        fun `Should Retrieve all Position (blocked - locked)`() {
            val expectedPosition1 = createPosition(blocked = true, locked = true)
            createPosition()
            createPosition(blocked = true)

            handle("/api/positions?blocked=true&locked=true") {
                assertThat(response)
                    .status(HttpStatusCode.OK)
                    .contentType(JsonUtf8)
                    .listBody<Position>()
                    .isEqualTo(
                        listOf(
                            expectedPosition1
                        )
                    )
            }
        }
    }


    @Nested
    inner class PositionUpdate {

        @Test
        fun `should block position`() {
            val expectedPosition = createPosition()

            handle("/api/positions/block/${expectedPosition.id}", HttpMethod.Patch) {
                assertThat(response)
                    .status(HttpStatusCode.OK)
                    .contentType(JsonUtf8)
                    .body<Position>()
                    .isEqualTo(
                        expectedPosition.copy(
                            blocked = true
                        )
                    )
            }
        }

        @Test
        fun `can't block position (not-found)`() {

            handle("/api/positions/block/any-id", HttpMethod.Patch) {
                assertThat(response)
                    .status(HttpStatusCode.NotFound)
                    .contentType(PlainUtf8)
                    .body<String>()
                    .isEqualTo("Position Not Found")
            }
        }

        @Test
        fun `should unblock position`() {
            val expectedPosition = createPosition(blocked = true)

            handle("/api/positions/unblock/${expectedPosition.id}", HttpMethod.Patch) {
                assertThat(response)
                    .status(HttpStatusCode.OK)
                    .contentType(JsonUtf8)
                    .body<Position>()
                    .isEqualTo(
                        expectedPosition.copy(
                            blocked = false
                        )
                    )
            }
        }

        @Test
        fun `can't unblock position (not-found)`() {

            handle("/api/positions/unblock/any-id", HttpMethod.Patch) {
                assertThat(response)
                    .status(HttpStatusCode.NotFound)
                    .contentType(PlainUtf8)
                    .body<String>()
                    .isEqualTo("Position Not Found")
            }
        }

    }


    @Nested
    inner class PositionCreate {

        @Test
        fun `should create position`() {
            handle(
                uri = "/api/positions",
                method = HttpMethod.Post,
                body = NewPosition(
                    portfolioId = "port-id",
                    balance = BigDecimal.valueOf(100),
                    positionType = "Money Account",
                    assetType = AssetType.EGP
                )
            ) {
                assertThat(response)
                    .status(HttpStatusCode.Created)
                    .contentType(JsonUtf8)
                    .body<Position>()
                    .isEqualToIgnoringGivenFields(
                        Position(
                            portfolioId = "port-id",
                            balance = BigDecimal.valueOf(10000,2),
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
    }
}