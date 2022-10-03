package Backend.FinalProject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoUserInfoDto {
    private String id;
    private String nickname;
    private String imgUrl;
    private String gender;
    private Integer minAge;
}