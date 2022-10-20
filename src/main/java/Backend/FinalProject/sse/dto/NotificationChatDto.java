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
public class NotificationChatDto {
    private Long id;
    private String notificationType;
    private Boolean status;

    public static NotificationChatDto create(Notification notification) {
        return new NotificationChatDto(notification.getId(), notification.getNotificationType().toString(),notification.getIsRead());
    }
}