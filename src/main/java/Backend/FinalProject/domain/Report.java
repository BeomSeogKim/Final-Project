package Backend.FinalProject.domain;

import Backend.FinalProject.domain.enums.ShowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

import static Backend.FinalProject.domain.ReportStatus.DONE;
import static Backend.FinalProject.domain.enums.ShowStatus.HIDE;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report extends Timestamped {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column
    private String content;

    private Long reportMemberId;

    private Long reportPostId;

    private Long memberId;

    private Long postId;

    private Long commentId;

    @Enumerated(value = STRING)
    private ReportStatus status;

    @Enumerated(value = STRING)
    private ShowStatus show;

    @Transactional
    public void updateStatus() {
        this.status = DONE;
    }

    @Transactional
    public void hide() {
        this.show = HIDE;
    }
}
