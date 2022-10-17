package Backend.FinalProject.sse.domain;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Timestamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Builder
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends Timestamped {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Embedded
    private NotificationContent notificationContent;    // 알림 내용 - 50자 이내

    @Embedded
    private RelatedUrl url;

    @Column(nullable = false)
    private Boolean isRead;                             // 읽었는지에 대한 여부

    @Enumerated(STRING)
    @Column(nullable = false)
    private NotificationType notificationType;      // 알림 종류

    @ManyToOne(fetch = LAZY)
    @OnDelete(action = CASCADE)
    @JoinColumn(name = "member_id")
    private Member member;

    public void read() {
        this.isRead = true;
    }

    public String getNotificationContent() {
        return notificationContent.getNotificationContent();
    }

    public String getUrl() {
        return url.getUrl();
    }
}
