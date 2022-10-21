package Backend.FinalProject.WebSocket.domain;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Timestamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
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

    @OneToMany(fetch = LAZY, mappedBy = "chatMessage", cascade = ALL)
    private List<ReadCheck> readCheckList;


    @Transactional
    public void addNumOfRead() {
        this.numOfRead++;
    }

    @Transactional
    public void setNumOfRead() {
        this.numOfRead = 1;
    }
}
