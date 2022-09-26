package Backend.FinalProject.dto.response;

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
    private String title;
    private String imgUrl;
    private String address;
    private LocalDate dDay;
}
