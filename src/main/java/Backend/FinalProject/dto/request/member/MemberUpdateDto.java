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
public class MemberUpdateDto {

    private String nickname;
    private MultipartFile imgFile;
}
