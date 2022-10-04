package Backend.FinalProject.WebSocket.domain;

import Backend.FinalProject.domain.Timestamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ChatRoom extends Timestamped {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String roomId;
    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy ="chatRoom", cascade = ALL, orphanRemoval = true)
    private List<ChatMember> chatMemberList;

    public static ChatRoom create(String name) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.roomId = UUID.randomUUID().toString();
        chatRoom.name = name;
        return chatRoom;
    }
}
