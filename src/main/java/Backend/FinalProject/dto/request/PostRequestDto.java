package Backend.FinalProject.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
public class PostRequestDto {
    private String title;
    private String address;
    private String content;
    private int maxNum;
    @DateTimeFormat(pattern = "yyyy-MM-dd-HH")
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd-HH")
    private String startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd-HH")
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd-HH")
    private String endDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd-HH")
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd-HH")
    private String dDay;
}
