package Backend.FinalProject.WebSocket.domain.dtos;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    private String sender;
    private String message;
    private String sendTime;
    private String img;
}
