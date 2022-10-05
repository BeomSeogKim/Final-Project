package Backend.FinalProject.domain;

import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static Backend.FinalProject.domain.enums.MarketingAgreement.MARKETING_DISAGREE;
import static Backend.FinalProject.domain.enums.RequiredAgreement.REQUIRED_DISAGREE;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends Timestamped{

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


    public void updatePassword(String password) {

        this.password = password;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateImage(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Member member = (Member) o;
        return id != null && Objects.equals(id, member.id);
    }

    public void deleteMember() {
        this.userId = UUID.randomUUID().toString();
        this.password = UUID.randomUUID().toString();
        this.nickname = "탈퇴한 회원입니다.";
        this.minAge = 0;
        this.imgUrl = "https://tommy-bucket-final.s3.ap-northeast-2.amazonaws.com/memberImage/6c6c20cf-7490-4d9e-b6f6-73c185a417dd%E1%84%80%E1%85%B5%E1%84%87%E1%85%A9%E1%86%AB%E1%84%8B%E1%85%B5%E1%84%86%E1%85%B5%E1%84%8C%E1%85%B5.webp";
        this.userRole = Authority.ROLE_GUEST;
        this.gender = Gender.NEUTRAL;
        this.requiredAgreement = REQUIRED_DISAGREE;
        this.marketingAgreement = MARKETING_DISAGREE;
    }
}
