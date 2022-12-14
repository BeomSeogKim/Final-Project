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
public class ApplicantResponseDto {
    private Long postId;
    private String nickname;
    private String imgUrl;
    private String title;
    private ApplicationState state;
}
