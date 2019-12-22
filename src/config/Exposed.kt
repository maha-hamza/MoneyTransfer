package config

import org.jetbrains.exposed.sql.Database

fun initExposed() {
    inject<Database>()
}