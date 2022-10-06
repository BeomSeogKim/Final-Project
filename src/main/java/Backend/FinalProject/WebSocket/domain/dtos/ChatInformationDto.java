package Backend.FinalProject.WebSocket.domain.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatInformationDto {
    private Long roomId;
    private String message;
}
