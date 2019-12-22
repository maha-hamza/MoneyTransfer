package abstracts

import config.objectMapper
import com.uchuhimo.konf.Config
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.*
import module
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.test.KoinTest
import topLevelConfigs

abstract class AbstractTest : KoinTest {

    val testConfig: Config = Config { topLevelConfigs.forEach(::addSpec) }
        .from.hocon.resource("application.conf")
        .from.hocon.resource("application-test.conf")
        .apply { modifyConfig(this) }

    open fun modifyConfig(config: Config) {}
    open fun mocks(): List<Module> = emptyList()
    @BeforeEach
    fun beforeEach() {
        startKoin {
            modules(
                config.modules(
                    testConfig,
                    mocks()
                )
            )
        }
    }

    @AfterEach
    fun afterEach() {
        stopKoin()
    }

    fun handle(
        uri: String,
        method: HttpMethod = HttpMethod.Get,
        body: Any? = null,
        block: TestApplicationCall.() -> Unit
    ) {
        withTestApp {
            handleRequest(method, uri) {
                body?.let {
                    addHeader(
                        HttpHeaders.ContentType, JsonUtf8
                            .toString()
                    )
                    setBody(objectMapper().writeValueAsString(it))
                }
            }.apply(block)
        }
    }

    private fun <T> withTestApp(block: TestApplicationEngine.() -> T): T {
        return withTestApplication({ module() }, block)
    }

}