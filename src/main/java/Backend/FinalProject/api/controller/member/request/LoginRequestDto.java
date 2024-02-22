package Backend.FinalProject.api.controller.member.request;

import lombok.Getter;

@Getter
public class LoginRequestDto {
    public String userId;
    public String password;
}
