package Backend.FinalProject.WebSocket.domain.dtos;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ChatMessageInfoDto {

    private String chatRoomTitle;
    private List<ChatMessageResponse> chatMessageList;
    private Integer totalPage;
    private Integer currentPage;
    private Long totalMessage;
    private boolean isFirstPage;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
}
