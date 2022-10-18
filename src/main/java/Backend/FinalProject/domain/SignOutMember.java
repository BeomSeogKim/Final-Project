package Backend.FinalProject.domain;

import Backend.FinalProject.domain.enums.Regulation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignOutMember extends Timestamped{

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String userId;

    private String password;

    private String nickname;

    @Column
    private Integer minAge;

    @Column(length = 1000)
    private String imgUrl;

    @Enumerated(value = STRING)
    private Regulation regulation;
}
