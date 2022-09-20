package Backend.FinalProject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    private String title;
    private String address;
    private String content;
    private int maxNum;
    private LocalDate startDate;
    private LocalDate endDate;
    private int expire;
    private String comment;
    private String imgPost;

}
