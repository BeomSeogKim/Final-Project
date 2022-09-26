package Backend.FinalProject.domain;

import Backend.FinalProject.domain.enums.PostState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Post extends Timestamped{

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String title;

    private String content;

    private int maxNum;

    private int currentNum;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate dDay;

    @Column(length = 1000)
    private String imgUrl;

    @Enumerated(value = STRING)
    private PostState status;
    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String address;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<Application> applicationList = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<WishList> wishLists = new ArrayList<>();


    public void updateJson(String title, String address, String content, int maxNum,
                           LocalDate startDate, LocalDate endDate, LocalDate dDay){
        this.title = title;
        this.address = address;
        this.content = content;
        this.maxNum = maxNum;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dDay = dDay;
    }

    public boolean validateMember(Member member) {
        return !this.member.equals(member);
    }

    public void updateImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
