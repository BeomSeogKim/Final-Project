package Backend.FinalProject.domain;

import Backend.FinalProject.domain.enums.PostState;
import Backend.FinalProject.dto.request.PostUpdateRequestDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.*;
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

    @OneToMany(mappedBy = "post")
    private List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<Application> applicationList = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<WishList> wishLists = new ArrayList<>();

    public void update(PostUpdateRequestDto PostUpdateRequestDto){
        this.title = PostUpdateRequestDto.getTitle();
        this.address = PostUpdateRequestDto.getAddress();
        this.content = PostUpdateRequestDto.getContent();
        this.maxNum = PostUpdateRequestDto.getMaxNum();


    }
    public void update2(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean validateMember(Member member) {
        return !this.member.equals(member);
    }
}
