package Backend.FinalProject.domain.token;

import Backend.FinalProject.domain.Timestamped;
import Backend.FinalProject.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class RefreshToken extends Timestamped {

    @Id
    private String id;

    @JoinColumn(name = "member_id", nullable = false)
    @OneToOne(fetch = LAZY)
    private Member member;

    @Column(nullable = false)
    private String keyValue;

    //== Refresh Token 재발급 ==//
    @Transactional
    public void updateValue(String token) {
        this.keyValue = token;
    }
}

