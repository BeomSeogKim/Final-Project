package Backend.FinalProject.dto.response;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.WishList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WishListDto {
    private Long member;
    private Long post;
    private String address;
    private LocalDate dDay;
}
