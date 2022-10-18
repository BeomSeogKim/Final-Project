package Backend.FinalProject.dto.response.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberPasswordUpdateDto {
    private String password;
    private String updatePassword;
    private String updatePasswordCheck;
}