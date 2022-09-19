package Backend.FinalProject.domain;

import Backend.FinalProject.domain.enums.Authority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.parameters.P;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userId;

    private String password;

    @Column(unique = true)
    private String nickname;

    @Column(length = 1000)
    private String imgUrl;

    @Enumerated(value = EnumType.STRING)
    private Authority userRole;

    @OneToMany(mappedBy = "member")
    private List<Post> postList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<WishList> wishLists = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Application> applicationList = new ArrayList<>();

    public void updatePassword(String password) {

        this.password = password;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateImage(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
