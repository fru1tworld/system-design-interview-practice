package htmldownloader.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class DateUtilsTest {

    @Test
    @DisplayName("1달 전 날짜가 정확히 계산되어야 한다")
    void shouldCalculateOneMonthAgoCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expected = now.minus(1, ChronoUnit.MONTHS);

        // When
        LocalDateTime oneMonthAgo = DateUtils.getOneMonthAgo();

        // Then
        // 테스트 실행 시간 차이로 인한 약간의 오차 허용 (1초)
        assertThat(oneMonthAgo).isCloseTo(expected, java.time.Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("1달 이내 날짜는 true를 반환해야 한다")
    void shouldReturnTrueForDateWithinOneMonth() {
        // Given
        LocalDateTime recentDate = LocalDateTime.now().minusDays(15); // 15일 전

        // When
        boolean isWithinOneMonth = DateUtils.isWithinOneMonth(recentDate);

        // Then
        assertThat(isWithinOneMonth).isTrue();
    }

    @Test
    @DisplayName("1달 이전 날짜는 false를 반환해야 한다")
    void shouldReturnFalseForDateOlderThanOneMonth() {
        // Given
        LocalDateTime oldDate = LocalDateTime.now().minusMonths(2); // 2달 전

        // When
        boolean isWithinOneMonth = DateUtils.isWithinOneMonth(oldDate);

        // Then
        assertThat(isWithinOneMonth).isFalse();
    }

    @Test
    @DisplayName("null 날짜는 false를 반환해야 한다")
    void shouldReturnFalseForNullDate() {
        // When
        boolean isWithinOneMonth = DateUtils.isWithinOneMonth(null);

        // Then
        assertThat(isWithinOneMonth).isFalse();
    }

    @Test
    @DisplayName("두 날짜 사이의 차이가 1달 이내인지 정확히 판단해야 한다")
    void shouldCheckDifferenceBetweenTwoDatesCorrectly() {
        // Given
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime toWithinMonth = LocalDateTime.of(2024, 1, 25, 0, 0); // 24일 차이
        LocalDateTime toOverMonth = LocalDateTime.of(2024, 2, 15, 0, 0);  // 1달 15일 차이

        // When & Then
        assertThat(DateUtils.isDifferenceWithinOneMonth(from, toWithinMonth)).isTrue();
        assertThat(DateUtils.isDifferenceWithinOneMonth(from, toOverMonth)).isFalse();
    }

    @Test
    @DisplayName("null 날짜가 포함된 경우 false를 반환해야 한다")
    void shouldReturnFalseForNullDatesInDifferenceCheck() {
        // Given
        LocalDateTime date = LocalDateTime.now();

        // When & Then
        assertThat(DateUtils.isDifferenceWithinOneMonth(null, date)).isFalse();
        assertThat(DateUtils.isDifferenceWithinOneMonth(date, null)).isFalse();
        assertThat(DateUtils.isDifferenceWithinOneMonth(null, null)).isFalse();
    }
}