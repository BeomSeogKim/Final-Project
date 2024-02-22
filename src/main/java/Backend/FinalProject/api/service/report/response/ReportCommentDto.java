package Backend.FinalProject.api.service.report.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportCommentDto {
    private Long reportId;
    private Long postId;
    private Long commentId;
    private String content;
    private String nickname;
    private String postUrl;
    private String memberUrl;
    private String reportCommentContent;
}
