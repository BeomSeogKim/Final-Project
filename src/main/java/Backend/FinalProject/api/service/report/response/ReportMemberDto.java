package Backend.FinalProject.api.service.report.response;

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
    private String reportNickname;
    private String memberImgUrl;
}
