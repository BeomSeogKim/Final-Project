package Backend.FinalProject.dto.response.post;

import Backend.FinalProject.domain.enums.Category;
import Backend.FinalProject.domain.enums.PostState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Enumerated;
import java.time.LocalDate;

import static javax.persistence.EnumType.STRING;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AllPostResponseDto {

    private Long id;
    private int maxNum;
    private String title;
    private String address;
    private Category category;
    private String restDay;
    private LocalDate dDay;
    private String imgUrl;
    @Enumerated(value = STRING)
    private PostState status;
    private String authorImgUrl;
    private String authorNickname;
    private int numOfComment;
    private int numOfWish;
}
