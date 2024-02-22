package Backend.FinalProject.domain.comment;

import Backend.FinalProject.domain.Timestamped;
import Backend.FinalProject.domain.enums.Regulation;
import Backend.FinalProject.domain.member.Member;
import Backend.FinalProject.domain.post.Post;
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
public class Comment extends Timestamped {

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

    //== 댯굴 수정 ==//
    public void editComment(String commentDto) {
        this.content = commentDto;
    }

    //== 댓글 제재 ==//
    @Transactional
    public void executeRegulation() {
        this.regulation = REGULATED;
        this.member.executeRegulation();
    }
}
