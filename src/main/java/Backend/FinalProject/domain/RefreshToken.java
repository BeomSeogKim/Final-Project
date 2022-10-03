package Backend.FinalProject.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @OneToOne(fetch = LAZY, orphanRemoval = true)
    private Member member;

    @Column(nullable = false)
    private String keyValue;

    public void updateValue(String token) {
        this.keyValue = token;
    }
}

