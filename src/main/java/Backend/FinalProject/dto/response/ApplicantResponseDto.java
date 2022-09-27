package Backend.FinalProject.dto.response;

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
    private String title;
    private ApplicationState state;
}
