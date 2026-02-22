package htmldownloader.util

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object DateUtils {

    fun getOneMonthAgo(): LocalDateTime {
        return LocalDateTime.now().minus(1, ChronoUnit.MONTHS)
    }

    fun isWithinOneMonth(dateTime: LocalDateTime?): Boolean {
        if (dateTime == null) return false
        return dateTime.isAfter(getOneMonthAgo())
    }

    fun isDifferenceWithinOneMonth(from: LocalDateTime?, to: LocalDateTime?): Boolean {
        if (from == null || to == null) return false
        return ChronoUnit.MONTHS.between(from, to) < 1
    }

    fun now(): LocalDateTime = LocalDateTime.now()
}
