package Backend.FinalProject.dto.request.member;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {
    private String userId;
    private String password;
    private String passwordCheck;
    private String nickname;
    private String gender;
    private Integer age;
    private String ageCheck;
    private String requiredAgreement;
    private String marketingAgreement;
    private MultipartFile imgFile;
}
