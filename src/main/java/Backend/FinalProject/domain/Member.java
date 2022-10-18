package Backend.FinalProject.domain;

import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static Backend.FinalProject.domain.enums.Authority.ROLE_GUEST;
import static Backend.FinalProject.domain.enums.Regulation.REGULATED;
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

    private int numOfRegulation;

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

    //== 비밀번호 업데이트 ==//
    public void updatePassword(String password) {
        this.password = password;
    }

    //== 닉네임 업데이트 ==//
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    //== 프로필 이미지 업데이트 ==//
    public void updateImage(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    //== 회원 탈퇴 ==//
    public void signOut() {
        this.userId = UUID.randomUUID().toString();
        this.password = UUID.randomUUID().toString();
        this.nickname = "탈퇴한 회원입니다.";
        this.minAge = 0;
        this.imgUrl = "https://tommy-bucket-final.s3.ap-northeast-2.amazonaws.com/memberImage/6c6c20cf-7490-4d9e-b6f6-73c185a417dd%E1%84%80%E1%85%B5%E1%84%87%E1%85%A9%E1%86%AB%E1%84%8B%E1%85%B5%E1%84%86%E1%85%B5%E1%84%8C%E1%85%B5.webp";
        this.userRole = ROLE_GUEST;
    }

    //== 회원 제재 ==//
    @Transactional
    public void executeRegulation() {
        this.numOfRegulation++;
        if (this.numOfRegulation >= 10) {
            this.regulation = REGULATED;
            this.userRole = ROLE_GUEST;     // 누적 신고 10회 이상인 경우 서비스 이용을 못하게 하도록 변경
        }
    }

    // TODO 필요한 것인지 Check
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
}
