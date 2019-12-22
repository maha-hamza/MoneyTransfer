package abstracts

import io.ktor.server.testing.TestApplicationResponse
import org.junit.jupiter.api.Assertions

class ProjectionAssertions : Assertions() {
    companion object {
        fun assertThat(actual: TestApplicationResponse) = TestApplicationResponseAssert.assertThat(actual)
    }
}