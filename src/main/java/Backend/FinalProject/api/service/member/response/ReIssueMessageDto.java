package Backend.FinalProject.api.service.member.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReIssueMessageDto {
    private String message;
    private long expiresAt;
}
