package urlshorten.common

import kotlin.random.Random

class Snowflake {
    companion object {
        private const val UNUSED_BITS = 1
        private const val EPOCH_BITS = 41
        private const val NODE_ID_BITS = 10
        private const val SEQUENCE_BITS = 12

        private const val MAX_NODE_ID = (1L shl NODE_ID_BITS) - 1
        private const val MAX_SEQUENCE = (1L shl SEQUENCE_BITS) - 1
    }

    private val nodeId: Long = Random.nextLong(MAX_NODE_ID + 1)
    private val startTimeMillis: Long = 1738886400000L  // 2025-02-07

    private var lastTimeMillis: Long = startTimeMillis
    private var sequence: Long = 0L

    @Synchronized
    fun nextId(): Long {
        var currentTimeMillis = System.currentTimeMillis()

        if (currentTimeMillis < lastTimeMillis) {
            throw IllegalStateException("Invalid Time")
        }

        if (currentTimeMillis == lastTimeMillis) {
            sequence = (sequence + 1) and MAX_SEQUENCE
            if (sequence == 0L) {
                currentTimeMillis = waitNextMillis(currentTimeMillis)
            }
        } else {
            sequence = 0
        }

        lastTimeMillis = currentTimeMillis

        return ((currentTimeMillis - startTimeMillis) shl (NODE_ID_BITS + SEQUENCE_BITS)) or
                (nodeId shl SEQUENCE_BITS) or
                sequence
    }

    private fun waitNextMillis(currentTimestamp: Long): Long {
        var timestamp = currentTimestamp
        while (timestamp <= lastTimeMillis) {
            timestamp = System.currentTimeMillis()
        }
        return timestamp
    }
}
