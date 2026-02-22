package urlshorten.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import urlshorten.domain.shortenurl.ShortenUrls

private lateinit var databaseInstance: Database

val Application.database: Database
    get() = databaseInstance

fun Application.configureDatabase() {
    val driver = environment.config.propertyOrNull("database.driver")?.getString()
        ?: "com.mysql.cj.jdbc.Driver"
    val url = environment.config.propertyOrNull("database.url")?.getString()
        ?: "jdbc:mysql://localhost:3307/fru1t_gsd08"
    val user = environment.config.propertyOrNull("database.user")?.getString()
        ?: "root"
    val password = environment.config.propertyOrNull("database.password")?.getString()
        ?: "fru1t"

    databaseInstance = Database.connect(
        url = url,
        driver = driver,
        user = user,
        password = password
    )

    transaction(databaseInstance) {
        SchemaUtils.create(ShortenUrls)
    }

    log.info("Database configured: $url")
}
