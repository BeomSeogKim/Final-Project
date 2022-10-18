package Backend.FinalProject.dto.response;

import Backend.FinalProject.domain.enums.SignUpRoot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoDto {
    private String nickname;
    private String userId;
    private String imgUrl;
    private SignUpRoot root;
}
