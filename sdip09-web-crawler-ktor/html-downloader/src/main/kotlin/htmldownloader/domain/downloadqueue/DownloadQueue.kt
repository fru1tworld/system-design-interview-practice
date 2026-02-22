package htmldownloader.domain.downloadqueue

import htmldownloader.util.DateUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object DownloadQueues : Table("download_queue") {
    val id = varchar("id", 36)
    val crawlingId = varchar("crawling_id", 36)
    val originalUrl = varchar("original_url", 65535)
    val rootDomain = varchar("root_domain", 255).nullable()
    val depth = integer("depth")
    val bfsPath = varchar("bfs_path", 65535)
    val status = varchar("status", 20)
    val score = long("score").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}

data class DownloadQueue(
    val id: String,
    val crawlingId: String,
    val originalUrl: String,
    val rootDomain: String?,
    val depth: Int,
    val bfsPath: String,
    val status: String,
    val score: Long?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun create(
            id: String,
            crawlingId: String,
            originalUrl: String,
            bfsPath: String,
            depth: Int,
            status: String
        ): DownloadQueue {
            val now = DateUtils.now()
            return DownloadQueue(
                id = id,
                crawlingId = crawlingId,
                originalUrl = originalUrl,
                rootDomain = null,
                depth = depth,
                bfsPath = bfsPath,
                status = status,
                score = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

enum class DownloadStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
}
