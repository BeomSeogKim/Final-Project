package Backend.FinalProject.WebSocket.domain;

import Backend.FinalProject.domain.Member;
import lombok.Getter;

import javax.persistence.*;
import java.util.UUID;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Entity
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String roomId;
    private String name;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public static ChatRoom create(String name) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.roomId = UUID.randomUUID().toString();
        chatRoom.name = name;
        return chatRoom;
    }
}
