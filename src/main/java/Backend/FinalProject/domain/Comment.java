package Backend.FinalProject.domain;

import Backend.FinalProject.domain.enums.Regulation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

import static Backend.FinalProject.domain.enums.Regulation.REGULATED;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends Timestamped{

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;


    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Enumerated(value = STRING)
    private Regulation regulation;

    //== 연관관계 메서드 ==//
    public void setMember(Member member) {
        this.member = member;
        member.getCommentList().add(this);
    }

    public void setPost(Post post) {
        this.post = post;
        post.getCommentList().add(this);
    }
    
    public void update(String commentDto) {
        this.content = commentDto;
    }

    @Transactional
    public void executeRegulation() {
        this.regulation = REGULATED;
        this.member.executeRegulation();
    }
}
