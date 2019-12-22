package config

import com.uchuhimo.konf.Config
import org.flywaydb.core.Flyway

fun runMigrations(config: Config) {
    Flyway
        .configure()
        .dataSource(
            config[HDB.connectionString],
            config[HDB.user],
            config[HDB.password]
        )
        .load()
        .migrate()
}