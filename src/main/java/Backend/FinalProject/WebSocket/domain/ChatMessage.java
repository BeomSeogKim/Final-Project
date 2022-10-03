package Backend.FinalProject.WebSocket.domain;

import Backend.FinalProject.domain.enums.MessageType;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private MessageType type;
    private String roomId;
    private String sender;
    private String message;
}
