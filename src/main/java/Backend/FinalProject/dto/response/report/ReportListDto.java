package Backend.FinalProject.dto.response.report;

import Backend.FinalProject.dto.response.report.ReportCommentDto;
import Backend.FinalProject.dto.response.report.ReportMemberDto;
import Backend.FinalProject.dto.response.report.ReportPostDto;
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
