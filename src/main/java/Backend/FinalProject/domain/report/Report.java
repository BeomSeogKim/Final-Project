package Backend.FinalProject.domain.report;

import Backend.FinalProject.domain.Timestamped;
import Backend.FinalProject.domain.enums.ReportStatus;
import Backend.FinalProject.domain.enums.ShowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

import static Backend.FinalProject.domain.enums.ReportStatus.DONE;
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
    private ReportStatus reportStatus;

    @Enumerated(value = STRING)
    private ShowStatus showStatus;

    //== 신고 완료 처리 ==//
    @Transactional
    public void updateStatus() {
        this.reportStatus = DONE;
    }

    //== 신고 숨기기 ==//
    @Transactional
    public void hide() {
        this.showStatus = HIDE;
    }
}
