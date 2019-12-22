package config

import com.uchuhimo.konf.Config
import fund_transfer.positions.PositionController
import fund_transfer.positions.PositionService
import fund_transfer.transfer.TransferController
import fund_transfer.transfer.TransferService
import org.jetbrains.exposed.sql.Database
import org.koin.Logger.slf4jLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import javax.sql.DataSource

fun initKoin(config: Config) {
    startKoin {
        modules(modules(config))
        slf4jLogger()
    }
}

fun modules(config: Config, overrides: List<Module> = emptyList()): List<Module> {
    return listOf(
        module {
            single { config }

            single { connectionPool(config) }
            single { Database.connect(get<DataSource>()) }
            
            single { PositionController() }
            single { PositionService() }

            single { TransferController() }
            single { TransferService() }

        }
    ) + overrides
}

inline fun <reified T : Any> inject(): T {
    return GlobalContext.get().koin.get()
}

inline fun <reified T : Any> lazyInject(): Lazy<T> {
    return lazy { inject<T>() }
}