package Backend.FinalProject.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

import static Backend.FinalProject.domain.ReportStatus.DONE;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

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

    @Transactional
    public void updateStatus() {
        this.status = DONE;
    }
}
