package Backend.FinalProject.domain;

import Backend.FinalProject.domain.enums.Authority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long member_id;

    @Column(unique = true)
    private String id;

    private String password;

    @Column(unique = true)
    private String nickname;

    private String img_url;

    @OneToMany(mappedBy = "member")
    private List<Post> posts;

    @Enumerated(value = EnumType.STRING)
    private Authority userRole;



}
