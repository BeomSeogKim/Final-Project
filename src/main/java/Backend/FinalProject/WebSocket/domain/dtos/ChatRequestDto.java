package Backend.FinalProject.WebSocket.domain.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequestDto {
    private Long roomId;
    private String message;
}
