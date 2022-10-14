package Backend.FinalProject.sse.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor
public class NotificationContent {
    private static final int Max_LENGTH = 50;

    @Column(nullable = false, length = Max_LENGTH)
    private String notificationContent;


    public NotificationContent(String notificationContent) throws Exception {
        if (isNotValidNotificationContent(notificationContent)) {
            throw new Exception();
        }
        this.notificationContent = notificationContent;
    }

    private boolean isNotValidNotificationContent(String notificationContent) {
        return Objects.isNull(notificationContent) || notificationContent.length() > Max_LENGTH
                || notificationContent.isEmpty();

    }
}
