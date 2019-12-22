package abstracts

import config.objectMapper
import io.ktor.http.ContentType
import io.ktor.http.HeaderValueParam
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationResponse
import kotlinx.coroutines.io.jvm.javaio.toInputStream
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.assertj.core.api.ObjectAssert


class TestApplicationResponseAssert(
    actual: TestApplicationResponse
) : AbstractAssert<TestApplicationResponseAssert, TestApplicationResponse>(
    actual,
    TestApplicationResponseAssert::class.java
) {

    fun status(
        status: HttpStatusCode,
        vararg statuses: HttpStatusCode
    ): TestApplicationResponseAssert {
        val allStatusCodes = statuses.toList()
            .plus(status)
            .map(HttpStatusCode::value)

        assertThat(allStatusCodes).contains(actual.status()!!.value)

        return this
    }

    fun contentType(expectation: ContentType): TestApplicationResponseAssert {
        assertThat(actual.headers[HttpHeaders.ContentType])
            .isEqualTo(expectation.toString())

        return this
    }

    inline fun <reified T : Any> body(): ObjectAssert<T> {
        val body = actual.body<T>()
        return assertThat(body)
    }

    inline fun <reified T : Any> bodyActual(): T {
        return actual.body<T>()
    }

    inline fun <reified T : Any> listBody(): ListAssert<T> {
        val body = actual.body<Array<T>>()
        return assertThat(body.toList())
    }

    inline fun <reified T : Any> listBodyActual(): List<T> {
        return actual.body<Array<T>>().toList()
    }

    companion object {
        fun assertThat(actual: TestApplicationResponse) = TestApplicationResponseAssert(actual)
    }

}

inline fun <reified T : Any> TestApplicationResponse.body(): T {
    if (T::class == String::class) {
        return this.content!! as T
    }

    val channel = contentChannel() ?: throw NullPointerException("response's byteReadChannel is null!")
    return objectMapper().readValue(channel.toInputStream(), T::class.java)
}

val JsonUtf8: ContentType
    get() = ContentType(
        contentType = "application",
        contentSubtype = "json",
        parameters = listOf(HeaderValueParam("charset", "UTF-8"))
    )

val PlainUtf8
    get() = ContentType(
        contentType = "text",
        contentSubtype = "plain",
        parameters = listOf(HeaderValueParam("charset", "UTF-8"))
    )
