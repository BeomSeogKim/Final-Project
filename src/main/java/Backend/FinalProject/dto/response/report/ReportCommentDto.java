package Backend.FinalProject.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportCommentDto {
    private Long postId;
    private Long reportId;
    private Long commentId;
    private String content;
    private String reportNickname;
    private String postUrl;
    private String memberUrl;
    private String reportCommentContent;
}
