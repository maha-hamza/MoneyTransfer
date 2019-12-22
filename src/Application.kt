import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import config.*
import fund_transfer.exception_handling.exceptionHandling
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.request.path
import io.ktor.routing.Routing
import io.ktor.server.engine.ApplicationEngineEnvironment
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import org.slf4j.event.Level

@KtorExperimentalAPI
fun main(args: Array<String>) {

    val config = loadConfig()
    runMigrations(config = config)
    val cmdEnv = buildAppEngineEnv(config)
    initKoin(config)
    initExposed()
    embeddedServer(Netty, cmdEnv)
        .start()

}

@KtorExperimentalAPI
fun buildAppEngineEnv(config: Config): ApplicationEngineEnvironment {
    return applicationEngineEnvironment {
        this.config = KonfApplicationConfig(config)
        connector {
            port = config[Ktor.Deployment.port]
        }
    }
}

fun Application.module() {
    install(Routing) { route() }
    install(StatusPages, exceptionHandling)
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
    }

    install(ContentNegotiation, jackson)
}

val topLevelConfigs = arrayOf(
    Ktor,
    HDB
)

object Ktor : ConfigSpec() {
    object Deployment : ConfigSpec() {
        val port by required<Int>()
    }

    object Application : ConfigSpec() {
        val modules by required<Array<String>>()
    }
}

object HDB : ConfigSpec() {
    val driverClassName by required<String>()
    val connectionString by required<String>()
    val user by required<String>()
    val password by required<String>()
}

fun loadConfig(): Config {
    return Config { topLevelConfigs.forEach(::addSpec) }
        .from.hocon.resource("application.conf")
}

