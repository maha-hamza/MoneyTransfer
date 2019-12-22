package abstracts

import config.initExposed
import config.lazyInject
import config.runMigrations
import mu.KLogging
import org.junit.jupiter.api.BeforeEach
import org.koin.core.KoinComponent
import javax.sql.DataSource

abstract class AbstractDBTest : KoinComponent, AbstractTest() {
    private val ds by lazyInject<DataSource>()

    @BeforeEach
    fun prepareDB() {
        logger.debug { "preparing DB..." }
        ds.connection.use {
            it.createStatement().executeUpdate("DROP ALL OBJECTS")
            runMigrations(testConfig)
            initExposed()
        }
        logger.debug { "DB prepared!" }
    }

    companion object : KLogging()
}