package Backend.FinalProject.Tool;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Time {

    private static class TIME_MAXIMUM {
        public static final int SEC = 60;
        public static final int MIN = 60;
        public static final int HOUR = 24;
        public static final int DAY = 30;
        public static final int MONTH = 12;
    }


    // Formatter
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // String -> LocalDateTime
    public LocalDateTime stringToLocalDateTime(String dateStr) {
        return LocalDateTime.parse(dateStr, formatter);
    }

    // String -> LocalDate
    public LocalDate stringToLocalDate(String dateStr) {
        return LocalDate.parse(dateStr, formatter);
    }


    // ~일전 ~초전으로 바꿔서 나타내주는 로직
    public String convertLocalDatetimeToTime(LocalDateTime localDateTime) {
        LocalDateTime now = LocalDateTime.now();

        long diffTime = localDateTime.until(now, ChronoUnit.SECONDS); // now 보다 이후면 +, 전이면 -

        if (diffTime < TIME_MAXIMUM.SEC){
            return diffTime + "초전";
        }
        diffTime = diffTime / TIME_MAXIMUM.SEC;
        if (diffTime < TIME_MAXIMUM.MIN ) {
            return diffTime + "분 전";
        }
        diffTime = diffTime / TIME_MAXIMUM.MIN;
        if (diffTime < TIME_MAXIMUM.HOUR) {
            return diffTime + "시간 전";
        }
        diffTime = diffTime / TIME_MAXIMUM.HOUR;
        if (diffTime < TIME_MAXIMUM.DAY) {
            return diffTime + "일 전";
        }
        diffTime = diffTime / TIME_MAXIMUM.DAY;
        if (diffTime < TIME_MAXIMUM.MONTH) {
            return diffTime + "개월 전";
        }
        diffTime = diffTime / TIME_MAXIMUM.MONTH;
        return diffTime + "년 전";
    }

    // ~일전 ~초전으로 바꿔서 나타내주는 로직
    public String convertLocalDateToTime(LocalDate localDate) {
        LocalDate now = LocalDate.now();

        long diffTime = now.until(localDate, ChronoUnit.DAYS); // now 보다 이후면 +, 전이면 -

        if (diffTime < TIME_MAXIMUM.DAY) {
            return diffTime + "일 전";
        }
        diffTime = diffTime / TIME_MAXIMUM.DAY;
        if (diffTime < TIME_MAXIMUM.MONTH) {
            return diffTime + "개월 전";
        }
        diffTime = diffTime / TIME_MAXIMUM.MONTH;
        return diffTime + "년 전";
    }

}
