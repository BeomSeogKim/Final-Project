package Backend.FinalProject.WebSocket.domain;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Timestamped;
import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
//@Setter
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

    @Column
    private int numOfRead;

    @OneToOne(fetch = LAZY, mappedBy = "chatMessage", cascade = ALL)
    private ReadCheck readCheck;

    public void setReadCheck(ReadCheck readCheck) {
        this.readCheck = readCheck;
    }

    @Transactional
    public void addNumOfRead() {
        this.numOfRead++;
    }

    @Transactional
    public void setNumOfRead() {
        this.numOfRead = 1;
    }
}
