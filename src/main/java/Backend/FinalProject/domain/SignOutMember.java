package Backend.FinalProject.domain;

import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignOutMember {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userId;

    private String password;

    private String nickname;

    @Column
    private Integer minAge;

    @Column(length = 1000)
    private String imgUrl;

    @Enumerated(value = STRING)
    private Authority userRole;

    @Enumerated(value = STRING)
    private SignUpRoot root;

    @Enumerated(value = STRING)
    private Gender gender;

    @Enumerated(value = STRING)
    private RequiredAgreement requiredAgreement;

    @Enumerated(value = STRING)
    private MarketingAgreement marketingAgreement;

    @Enumerated(value = STRING)
    private AgeCheck ageCheck;

    @Enumerated(value = STRING)
    private Regulation regulation;

    @OneToMany(mappedBy = "member", cascade = ALL, orphanRemoval = true)
    private List<ChatMember> chatMember;

    @OneToMany(mappedBy = "member")
    private List<Post> postList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<WishList> wishLists = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Application> applicationList = new ArrayList<>();
}
