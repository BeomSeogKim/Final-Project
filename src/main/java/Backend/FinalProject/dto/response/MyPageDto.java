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
public class MyPageDto {
    private String title;
    private String address;
    private LocalDate dDay;
}
