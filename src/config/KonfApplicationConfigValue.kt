package config

import io.ktor.config.ApplicationConfigValue
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class KonfApplicationConfigValue(
    private val value: Any
) : ApplicationConfigValue {

    @KtorExperimentalAPI
    @Suppress("UNCHECKED_CAST")
    override fun getList(): List<String> {
        return when (value) {
            is String -> listOf(value)
            is List<*> -> when {
                value.isEmpty() -> emptyList()
                value[0] is String -> value as List<String>
                else -> value.map { it.toString() }
            }
            is Array<*> -> when {
                value.isEmpty() -> emptyList()
                value.isArrayOf<String>() -> value.toList() as List<String>
                else -> value.map { it.toString() }
            }
            else -> {
                println(value.javaClass); throw RuntimeException("oops!")
            }
        }
    }

    @KtorExperimentalAPI
    override fun getString(): String {
        return value.toString()
    }
}