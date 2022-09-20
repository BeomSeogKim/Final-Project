package Backend.FinalProject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int expire;
    private String comment;
    private String imgPost;

}
