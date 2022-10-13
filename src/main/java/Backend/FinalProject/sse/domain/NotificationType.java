package Backend.FinalProject.sse.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    APPLY, ACCEPT,
    REJECT, REPLY;

}
