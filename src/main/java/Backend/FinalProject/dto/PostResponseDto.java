package Backend.FinalProject.dto;

import Backend.FinalProject.domain.enums.Category;
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
    private Category category;
    private String authorId;
    private String authorNickname;
    private String memberImgUrl;
    private long memberId;
    private String address;
    private String placeX;
    private String placeY;
    private String placeUrl;
    private String placeName;
    private String detailAddress;
    private String content;
    private int maxNum;
    private int currentNum;
    private String restDay;
    private LocalDate dDay;
    private String postImgUrl;
    private List<CommentResponseDto> commentList;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isWish;

}
