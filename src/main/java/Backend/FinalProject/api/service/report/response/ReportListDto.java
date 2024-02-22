package Backend.FinalProject.api.service.report.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportListDto {
    private List<ReportMemberDto> memberList;
    private List<ReportPostDto> postList;
    private List<ReportCommentDto> commentList;

}
