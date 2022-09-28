package Backend.FinalProject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponseDto {
    private String title;
    private int currentNum;
    private int maxNum;
    private List<ApplicationListResponseDto> applicants;
}
