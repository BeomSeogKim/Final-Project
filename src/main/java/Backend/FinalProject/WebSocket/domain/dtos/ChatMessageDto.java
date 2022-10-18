package Backend.FinalProject.WebSocket.domain.dtos;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChatMessageDto {

    private String sender;
    private String message;
    private String sendTime;
    private String senderId;
    private String imgUrl;
}
