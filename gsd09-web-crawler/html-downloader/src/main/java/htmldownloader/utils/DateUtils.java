package htmldownloader.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    /**
     * 현재 시간에서 1달 전 시간을 반환
     * @return 1달 전 LocalDateTime
     */
    public static LocalDateTime getOneMonthAgo() {
        return LocalDateTime.now().minus(1, ChronoUnit.MONTHS);
    }

    /**
     * 주어진 날짜가 1달 이내인지 확인
     * @param dateTime 확인할 날짜
     * @return 1달 이내면 true, 그렇지 않으면 false
     */
    public static boolean isWithinOneMonth(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isAfter(getOneMonthAgo());
    }

    /**
     * 두 날짜 사이의 차이가 1달 이내인지 확인
     * @param from 시작 날짜
     * @param to 끝 날짜
     * @return 1달 이내면 true
     */
    public static boolean isDifferenceWithinOneMonth(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return false;
        }
        return ChronoUnit.MONTHS.between(from, to) < 1;
    }
}