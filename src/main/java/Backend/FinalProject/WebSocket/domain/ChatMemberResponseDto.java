package Backend.FinalProject.WebSocket.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMemberResponseDto {
    private String imgUrl;
    private String nickname;
    private Long memberId;
    private boolean isLeader;
}
