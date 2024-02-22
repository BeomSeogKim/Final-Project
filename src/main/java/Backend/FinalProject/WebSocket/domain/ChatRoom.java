package Backend.FinalProject.WebSocket.domain;

import Backend.FinalProject.domain.Timestamped;
import Backend.FinalProject.domain.post.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ChatRoom extends Timestamped {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    private int numOfMember;

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @OneToMany(mappedBy ="chatRoom", cascade = ALL, orphanRemoval = true)
    private List<ChatMember> chatMemberList;

    private LocalDateTime lastChatTime;

    //== 연관관계 메서드 ==//
    public void setPost(Post post) {
        this.post = post;
    }

    public void addMember() {
        this.numOfMember++;
    }

    public void updateName(String title) {
        this.name = title;
    }

    public void updateTime(LocalDateTime lastChatTime) {
        this.lastChatTime = lastChatTime;
    }
}
