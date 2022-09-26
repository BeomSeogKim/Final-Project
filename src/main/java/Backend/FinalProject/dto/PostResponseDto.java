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
    private String author;
    private String memberImgUrl;
    private String address;
    private String content;
    private int maxNum;
    private String restDay;
    private LocalDate dDay;
    private String postImgUrl;
    private List<CommentResponseDto> commentList;
    private LocalDate startDate;
    private LocalDate endDate;

}
