package Backend.FinalProject.dto.response.application;

import Backend.FinalProject.domain.enums.ApplicationState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationListResponseDto {
    private String nickname;
    private String imgUrl;
    private ApplicationState state;
    private String content;
    private Long applicationId;
    private Long postId;
    private Long applicationMemberId;
}
