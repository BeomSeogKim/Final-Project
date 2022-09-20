package Backend.FinalProject.domain;

import Backend.FinalProject.domain.enums.PostState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue
    @Column(name = "post_id")
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
}
