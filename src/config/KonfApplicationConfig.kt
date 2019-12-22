package config

import io.ktor.config.ApplicationConfig
import io.ktor.config.ApplicationConfigValue
import io.ktor.util.KtorExperimentalAPI
import com.uchuhimo.konf.Config

@KtorExperimentalAPI
class KonfApplicationConfig(
    private val config: Config,
    private val prefix: String? = null
) : ApplicationConfig {

    @KtorExperimentalAPI
    override fun config(path: String): ApplicationConfig {
        val combinedPath = prefix?.let { "$it.path" } ?: path
        return when (config.contains(combinedPath)) {
            true -> KonfApplicationConfig(config, combinedPath)
            else -> throw RuntimeException("path not found $combinedPath")
        }
    }

    @KtorExperimentalAPI
    override fun configList(path: String): List<ApplicationConfig> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    @KtorExperimentalAPI
    override fun property(path: String): ApplicationConfigValue {
        val combinedPath = prefix?.let { "$it.path" } ?: path
        return KonfApplicationConfigValue(config[combinedPath])
    }

    @KtorExperimentalAPI
    override fun propertyOrNull(path: String): ApplicationConfigValue? {
        val combinedPath = prefix?.let { "$it.path" } ?: path
        return when (config.contains(combinedPath)) {
            true -> property(path)
            else -> null
        }
    }
}