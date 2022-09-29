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
public class MyActPostResponseDto {
    private Long postId;
    private String title;
    private String imgUrl;
    private ApplicationState state;
}
