package Backend.FinalProject.api.service.comment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AllCommentResponseDto {
    private Long commentId;
    private String memberImage;
    private String memberNickname;
    private String content;
    private String memberId;
    private Long commentMemberId;
}