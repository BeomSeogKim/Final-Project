package Backend.FinalProject.dto.response;

import Backend.FinalProject.domain.SignUpRoot;
import Backend.FinalProject.domain.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class getMyPageDto {

    private String nickname;
    private String imgUrl;
    private SignUpRoot root;
    private Gender gender;
    private Integer minAge;
    private int aplicationCount;
    private int leaderCount;

}
