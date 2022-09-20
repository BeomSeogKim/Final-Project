package Backend.FinalProject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    private Long id;
    private String title;
    private String address;
    private String content;
    private int maxNum;
    private LocalDate startDate;
    private LocalDate endDate;
    private String expire;
    private String imgPost;
    private List<CommentResponseDto> commentResponseDtoList;

}
