package Backend.FinalProject.dto.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostRequestDto {
    private String title;
    private String address;
    private String content;
    private int maxNum;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String img;
    private String comment;


}
