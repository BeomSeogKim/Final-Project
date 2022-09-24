package Backend.FinalProject.dto.response;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WishListDto {
    private Long id;
    private Member member;
    private Post post;

}
