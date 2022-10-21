package Backend.FinalProject.WebSocket.domain;

import Backend.FinalProject.domain.Timestamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Slf4j
public class ReadCheck extends Timestamped {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "chat_member", nullable = false)
    private ChatMember chatMember;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "chat_message_id")
    private ChatMessage chatMessage;


    //== 연관관계 메서드 ==//

    public void setChatMember(ChatMember chatMember) {
        this.chatMember = chatMember;
        chatMember.getReadCheckList().add(this);
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
        chatMessage.getReadCheckList().add(this);
    }
}
