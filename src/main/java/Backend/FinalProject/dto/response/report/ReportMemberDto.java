package Backend.FinalProject.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportMemberDto {
    private Long reportId;
    private Long memberId;
    private String content;
}
