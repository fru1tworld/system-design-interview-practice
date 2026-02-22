package htmldownloader.domain.downloadqueue

import htmldownloader.util.DateUtils
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class DownloadQueueRepository(private val database: Database) {

    fun save(downloadQueue: DownloadQueue): DownloadQueue {
        transaction(database) {
            DownloadQueues.insert { row ->
                row[id] = downloadQueue.id
                row[crawlingId] = downloadQueue.crawlingId
                row[originalUrl] = downloadQueue.originalUrl
                row[rootDomain] = downloadQueue.rootDomain
                row[depth] = downloadQueue.depth
                row[bfsPath] = downloadQueue.bfsPath
                row[status] = downloadQueue.status
                row[score] = downloadQueue.score
                row[createdAt] = downloadQueue.createdAt
                row[updatedAt] = downloadQueue.updatedAt
            }
        }
        return downloadQueue
    }

    fun findById(id: String): DownloadQueue? {
        return transaction(database) {
            DownloadQueues.selectAll().where { DownloadQueues.id eq id }
                .map { it.toDownloadQueue() }
                .singleOrNull()
        }
    }

    fun updateStatus(id: String, status: String) {
        transaction(database) {
            DownloadQueues.update({ DownloadQueues.id eq id }) { row ->
                row[DownloadQueues.status] = status
                row[updatedAt] = DateUtils.now()
            }
        }
    }

    fun updateRootDomain(id: String, rootDomain: String) {
        transaction(database) {
            DownloadQueues.update({ DownloadQueues.id eq id }) { row ->
                row[DownloadQueues.rootDomain] = rootDomain
                row[updatedAt] = DateUtils.now()
            }
        }
    }

    fun existsByBfsPath(bfsPath: String): Boolean {
        return transaction(database) {
            DownloadQueues.selectAll().where { DownloadQueues.bfsPath eq bfsPath }
                .count() > 0
        }
    }

    fun existsByCrawlingIdAndBfsPath(crawlingId: String, bfsPath: String): Boolean {
        return transaction(database) {
            DownloadQueues.selectAll().where {
                (DownloadQueues.crawlingId eq crawlingId) and (DownloadQueues.bfsPath eq bfsPath)
            }.count() > 0
        }
    }

    fun existsByOriginalUrlWithinOneMonth(originalUrl: String, oneMonthAgo: LocalDateTime): Boolean {
        return transaction(database) {
            DownloadQueues.selectAll().where {
                (DownloadQueues.originalUrl eq originalUrl) and (DownloadQueues.createdAt greater oneMonthAgo)
            }.count() > 0
        }
    }

    fun findRootDomainByCrawlingId(crawlingId: String): String? {
        return transaction(database) {
            DownloadQueues.selectAll().where {
                (DownloadQueues.crawlingId eq crawlingId) and (DownloadQueues.depth eq 0)
            }.map { it[DownloadQueues.rootDomain] }
                .firstOrNull()
        }
    }

    private fun ResultRow.toDownloadQueue(): DownloadQueue = DownloadQueue(
        id = this[DownloadQueues.id],
        crawlingId = this[DownloadQueues.crawlingId],
        originalUrl = this[DownloadQueues.originalUrl],
        rootDomain = this[DownloadQueues.rootDomain],
        depth = this[DownloadQueues.depth],
        bfsPath = this[DownloadQueues.bfsPath],
        status = this[DownloadQueues.status],
        score = this[DownloadQueues.score],
        createdAt = this[DownloadQueues.createdAt],
        updatedAt = this[DownloadQueues.updatedAt]
    )
}
