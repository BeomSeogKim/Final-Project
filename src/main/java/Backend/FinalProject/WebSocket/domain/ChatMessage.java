package Backend.FinalProject.WebSocket.domain;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Timestamped;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage extends Timestamped {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column
    private String message;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column
    private String sendTime;
}