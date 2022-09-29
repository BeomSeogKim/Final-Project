package Backend.FinalProject.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequestDto {
    private String title;
    private String content;
    private int maxNum;
    private String address;
    private long placeX;
    private long placeY;
    private String placeUrl;
    private String placeName;
    private String detailAddress;

    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd")
    private String startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd")
    private String endDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd")
    private String dDay;

    private MultipartFile imgFile;

}
