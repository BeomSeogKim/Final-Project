package Backend.FinalProject.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequestDto {

    private String title;

    private String content;

    private int max_num;

    private LocalDateTime start_date;

    private LocalDateTime end_date;

    private String img_url;

    private String address;

    private String d_day;

}
