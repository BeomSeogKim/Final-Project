package Backend.FinalProject.domain;

import Backend.FinalProject.domain.enums.PostState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Post extends Timestamped{

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    private String content;

    private int maxNum;

    private int currentNum;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String imgPost;

    @Enumerated(EnumType.STRING)
    private PostState status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String address;

    private String dDay;

    @OneToMany(mappedBy = "post")
    private List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<Application> applicationList = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<WishList> wishLists = new ArrayList<>();

}
