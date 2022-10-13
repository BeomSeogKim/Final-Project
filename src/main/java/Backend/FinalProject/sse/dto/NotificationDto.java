package Backend.FinalProject.sse.dto;

import Backend.FinalProject.sse.domain.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class NotificationDto {
    private Long id;
    private String notificationContent;
    private Boolean status;

    public static NotificationDto create(Notification notification) {
        return new NotificationDto(notification.getId(), notification.getNotificationContent(),
                 notification.getIsRead());
    }
}
