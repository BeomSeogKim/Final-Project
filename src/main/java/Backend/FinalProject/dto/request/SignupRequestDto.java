package Backend.FinalProject.dto.request;


import lombok.Getter;

@Getter
public class SignupRequestDto {
    private String userId;
    private String password;
    private String passwordCheck;
    private String nickname;
}
