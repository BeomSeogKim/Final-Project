package Backend.FinalProject.domain;

import Backend.FinalProject.domain.enums.ApplicationState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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


    public void approve() {
        this.status = ApplicationState.APPROVED;
        this.post.plusCurrentNum();
    }

    public void disapprove() {
        if (this.status.equals(ApplicationState.APPROVED)) {
            this.post.minusCurrentNum();
        }
        this.status = ApplicationState.DENIED;
    }
}
