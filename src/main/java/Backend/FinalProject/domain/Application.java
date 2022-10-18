package Backend.FinalProject.domain;

import Backend.FinalProject.domain.enums.ApplicationState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static Backend.FinalProject.domain.enums.ApplicationState.*;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application extends Timestamped{

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Enumerated(value = STRING)
    private ApplicationState status;

    private String content;
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
        member.getApplicationList().add(this);
    }

    public void setPost(Post post) {
        this.post = post;
        post.getApplicationList().add(this);
    }

    //== 신청 승인 ==//
    public void approve() {
        this.status = APPROVED;
        this.post.plusCurrentNum();
    }

    //== 신청 거절 ==//
    public void disapprove() {
        if (this.status.equals(APPROVED)) {
            this.post.minusCurrentNum();
        }
        this.status = DENIED;
    }
}
