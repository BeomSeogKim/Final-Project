package Backend.FinalProject.dto;

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
}
