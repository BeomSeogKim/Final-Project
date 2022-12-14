package Backend.FinalProject.domain;

import Backend.FinalProject.domain.enums.Category;
import Backend.FinalProject.domain.enums.PostState;
import Backend.FinalProject.domain.enums.Regulation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static Backend.FinalProject.domain.enums.PostState.*;
import static Backend.FinalProject.domain.enums.Regulation.REGULATED;
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

    // 지번 주소
    private String address;
    // x 위치
    private String placeX;
    // y 위치
    private String placeY;
    // kakaoMap 링크
    private String placeUrl;
    // 장소 이름
    private String placeName;
    // 상세 주소
    private String detailAddress;
    // 모임의 카테고리
    @Enumerated(value = STRING)
    private Category category;

    @Enumerated(value = STRING)
    private Regulation regulation;

    private int numOfWish;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Application> applicationList = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WishList> wishLists = new ArrayList<>();


    //== 게시글 업데이트 ==//
    public void updatePost(Category category, String title, String address, String content, int maxNum,
                           String placeX, String placeY, String placeUrl, String placeName, String detailAddress,
                           LocalDate startDate, LocalDate endDate, LocalDate dDay){
        this.category = category;
        this.title = title;
        this.address = address;
        this.content = content;
        this.maxNum = maxNum;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dDay = dDay;
        this.placeX = placeX;
        this.placeY = placeY;
        this.placeUrl = placeUrl;
        this.placeName = placeName;
        this.detailAddress = detailAddress;
    }

    //== 연관관계 메서드 ==//
    public void setMember(Member member) {
        this.member = member;
        member.getPostList().add(this);
    }

    //== 게시글 이미지 변경 ==//
    public void updateImg(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    //== 모집 상태 변경 ==//
    public void updateStatus() {
        this.status = DONE;
    }

    //== 모집 상태 변경 ==//
    public void closeStatus() {
        this.status = CLOSURE;
    }

    //== 게시글 참여 인원 증가 ==//
    public void plusCurrentNum() {
        this.currentNum++;
        if (currentNum == this.maxNum) {
            this.status = DONE;
        }
    }

    //== 게시글 참여 인원 감소 ==//
    public void minusCurrentNum() {
        if (this.status != CLOSURE) {
            this.currentNum--;
            if (this.currentNum < this.maxNum) {
                this.status = RECRUIT;
            }
        }

    }

    //== 게시글 찜하기 ==//
    public void addWish() {
        numOfWish++;
    }

    //== 게시글 찜하기 취소 ==//
    public void removeWish() {
        numOfWish--;
    }

    //== 게시글 마감 (모임 진행 X) ==//
    public void disclose() {
        this.status = CLOSURE;
    }

    //== 게시글 제재 가하기 ==//
    @Transactional
    public void executeRegulation() {
        this.regulation = REGULATED;
        this.member.executeRegulation();
    }
}
