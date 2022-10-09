package Backend.FinalProject.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class WishList extends Timestamped{

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    //== 연관관계 메서드 ==//
    public void setMember(Member member) {
        this.member = member;
        member.getWishLists().add(this);
    }

    public void setPost(Post post) {
        this.post = post;
        post.getWishLists().add(this);
    }
}
