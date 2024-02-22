package Backend.FinalProject.api.service.post.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PostResponseDtoPage {
    List<AllPostResponseDto> postList;
    private Integer totalPage;
    private Integer currentPage;
    private Long totalPost;
    private boolean isFirstPage;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
}
