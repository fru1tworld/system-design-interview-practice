package htmldownloader.plugins

import htmldownloader.domain.downloadqueue.DownloadQueues
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

private lateinit var databaseInstance: Database

val Application.database: Database
    get() = databaseInstance

fun Application.configureDatabase() {
    val driver = environment.config.propertyOrNull("database.driver")?.getString()
        ?: "org.h2.Driver"
    val url = environment.config.propertyOrNull("database.url")?.getString()
        ?: "jdbc:h2:mem:webcrawler;DB_CLOSE_DELAY=-1"
    val user = environment.config.propertyOrNull("database.user")?.getString()
        ?: "sa"
    val password = environment.config.propertyOrNull("database.password")?.getString()
        ?: ""

    databaseInstance = Database.connect(
        url = url,
        driver = driver,
        user = user,
        password = password
    )

    transaction(databaseInstance) {
        SchemaUtils.create(DownloadQueues)
    }

    log.info("Database configured: $url")
}
