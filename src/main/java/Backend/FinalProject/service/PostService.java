package Backend.FinalProject.service;

import Backend.FinalProject.Tool.Time;
import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.domain.*;
import Backend.FinalProject.domain.enums.PostState;
import Backend.FinalProject.dto.CommentResponseDto;
import Backend.FinalProject.dto.PostResponseDto;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.PostRequestDto;
import Backend.FinalProject.dto.request.PostUpdateRequestDto;
import Backend.FinalProject.dto.response.AllPostResponseDto;
import Backend.FinalProject.repository.CommentRepository;
import Backend.FinalProject.repository.FilesRepository;
import Backend.FinalProject.repository.PostRepository;
import Backend.FinalProject.repository.WishListRepository;
import Backend.FinalProject.sercurity.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.now;

@RestController
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    private final TokenProvider tokenProvider;

    private final AmazonS3Service amazonS3Service;

    private final CommentRepository commentRepository;

    private final WishListRepository wishListRepository;

    private final FilesRepository filesRepository;
    private final Validation validation;
    Time time = new Time();

    String folderName = "/postImage";
    String baseImage = "https://s3.ap-northeast-2.amazonaws.com/tommy-bucket-final/postImage/948c5814-3746-4873-95ce-6743aeeec07bbaseImage.jpeg";

    // 게시글 등록
    @Transactional
    public ResponseDto<?> createPost(PostRequestDto request, HttpServletRequest httpServletRequest) {

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.validateCheck(httpServletRequest);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        String title = request.getTitle();
        String address = request.getAddress();
        String content = request.getContent();
        int maxNum = request.getMaxNum();
        MultipartFile imgFile = request.getImgFile();
        String imgUrl;

        if (title == null || address == null || content == null || maxNum == 0 ||
                request.getStartDate() == null || request.getEndDate() == null || request.getDDay() == null) {
            return ResponseDto.fail("NULL_DATA", "입력값을 다시 확인해주세요");
        } else if (title.trim().isEmpty() || address.trim().isEmpty() || content.trim().isEmpty()) {
            return ResponseDto.fail("EMPTY_DATA", "빈칸을 채워주세요");
        }
        // 최대 정원의 수는 최소 3명에서 최대 5명
        if (maxNum <= 2 || maxNum >= 6) {
            return ResponseDto.fail("MAXNUM ERROR", "모집 정원을 다시 확인해주세요");
        }

        // 날짜 String 을 LocalDate 로 변경
        LocalDate startDate, endDate, dDay;
        try {
            startDate = time.stringToLocalDate(request.getStartDate());
            endDate = time.stringToLocalDate(request.getEndDate());
            dDay = time.stringToLocalDate(request.getDDay());
        } catch (Exception e) {
            return ResponseDto.fail("INVALID TYPE", "날짜 형식을 확인해주세요");
        }

        // 모집 시작 날짜가 현재보다 이전일 경우 에러 처리
        if (startDate.isBefore(now()) || endDate.isBefore(now()) || dDay.isBefore(now())) {
            return ResponseDto.fail("WRONG DATE", "현재보다 이전 날짜를 택할 수 없습니다.");
        }

        // 모집 마감일자, 모집일이 모집 시작일자보다 이전일 경우 에러처리
        if (endDate.isBefore(startDate) || dDay.isBefore(startDate)) {
            return ResponseDto.fail("WRONG DATE", "날짜 선택을 다시 해주세요");
        }

        // 모집 일자가 모집 마감일보다 이전일 경우 에러처리
        if (dDay.isBefore(endDate)) {
            return ResponseDto.fail("WRONG DATE", "날짜 선택을 다시 해주세요");
        }


        // 이미지 업로드 관련 로직
        if (imgFile == null || imgFile.isEmpty()) {
            imgUrl = baseImage;
        } else {
            ResponseDto<?> image = amazonS3Service.uploadFile(imgFile, folderName);
            ImageFile imageFile = (ImageFile) image.getData();
            imgUrl = imageFile.getUrl();
        }

        Post post = Post.builder()
                .title(title)
                .content(content)
                .maxNum(maxNum)
                .currentNum(0)              // 현재 모집된 정원의 수
                .startDate(startDate)
                .endDate(endDate)
                .imgUrl(imgUrl)
                .status(PostState.RECRUIT)      // 현재 모집 중
                .member(member)
                .address(address)
                .dDay(dDay)                      // 남은 모집일자
                .build();

        postRepository.save(post);


        return ResponseDto.success("게시글 작성이 완료되었습니다.");
    }

    // 게시글 전체 조회
    public ResponseDto<?> getAllPost() {

        List<Post> all = postRepository.findAll();
        List<AllPostResponseDto> PostResponseDtoList = new ArrayList<>();

        for (Post post : all) {
            if (post.getStatus().equals(PostState.RECRUIT)) {
                PostResponseDtoList.add(
                        AllPostResponseDto.builder()
                                .id(post.getId())
                                .title(post.getTitle())
                                .maxNum(post.getMaxNum())
                                .address(post.getAddress())
                                .restDay(time.convertLocalDateToTime((post.getEndDate())))
                                .dDay(post.getDDay())
                                .imgUrl(post.getImgUrl())
                                .status(post.getStatus())
                                .build()
                );
            }
        }
        return ResponseDto.success(PostResponseDtoList);
    }

    // 게시글 상세 조회
    public ResponseDto<?> getPost(Long postId) {

        boolean isWish = false;

        Post post = isPresentPost(postId);   // 입력한 id에 해당하는 post가 있는지 검사 하는 과정
        if (null == post) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
        }
        List<Comment> commentList = commentRepository.findAllByPost(post);
        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();

        for (Comment comment : commentList) {
            commentResponseDtoList.add(
                    CommentResponseDto.builder()
                            .id(comment.getId())
                            .nickname(comment.getMember().getNickname())
                            .content(comment.getContent())
                            .createdAt(comment.getCreatedAt())
                            .build()
            );
        }

        Member member = tokenProvider.getMemberFromAuthentication();
        List<WishList> peopleList = wishListRepository.findAllByPostId(postId).orElse(null);
        if (member != null) {
            for (WishList wishList : peopleList) {
                if (wishList.getMember().getUserId()
                        .equals(member.getUserId())) {
                    isWish = true;
                }
            }
        }


        return ResponseDto.success(PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .authorId(post.getMember().getUserId())
                .authorNickname(post.getMember().getNickname())
                .memberImgUrl(post.getMember().getImgUrl())
                .address(post.getAddress())
                .content(post.getContent())
                .maxNum(post.getMaxNum())
                .currentNum(post.getCurrentNum())
                .restDay(time.convertLocalDateToTime(post.getEndDate()))
                .dDay(post.getDDay())
                .postImgUrl(post.getImgUrl())
                .startDate(post.getStartDate())
                .endDate(post.getEndDate())
                .commentList(commentResponseDtoList)
                .isWish(isWish)
                .build()
        );

    }

    @Transactional  // 게시글 업데이트
    public ResponseDto<?> updatePost(Long id, PostUpdateRequestDto postUpdateRequestDto, HttpServletRequest request) {

        ResponseDto<?> responseDto = validation.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();


        Post post = isPresentPost(id);
        if (null == post) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
        }

        String title = postUpdateRequestDto.getTitle();
        String address = postUpdateRequestDto.getAddress();
        String content = postUpdateRequestDto.getContent();
        int maxNum = postUpdateRequestDto.getMaxNum();
        MultipartFile imgFile = postUpdateRequestDto.getImgFile();
        String imgUrl;

        // 최대 정원은 3명에서 5명
        if (maxNum <= 2 || maxNum >= 6) {
            return ResponseDto.fail("MAXNUM ERROR", "모집 정원을 다시 확인해주세요");
        }

        // 날짜 String 을 LocalDate 로 변경
        LocalDate startDate, endDate, dDay;
        try {
            startDate = time.stringToLocalDate(postUpdateRequestDto.getStartDate());
            endDate = time.stringToLocalDate(postUpdateRequestDto.getEndDate());
            dDay = time.stringToLocalDate(postUpdateRequestDto.getDDay());
        } catch (Exception e) {
            return ResponseDto.fail("INVALID TYPE", "날짜 형식을 확인해주세요");
        }

        // 모집 시작 날짜가 현재보다 이전일 경우 에러 처리
        if (startDate.isBefore(now()) || endDate.isBefore(now()) || dDay.isBefore(now())) {
            return ResponseDto.fail("WRONG DATE", "현재보다 이전 날짜를 택할 수 없습니다.");
        }

        // 모집 마감일자, 모집일이 모집 시작일자보다 이전일 경우 에러처리
        if (endDate.isBefore(startDate) || dDay.isBefore(startDate)) {
            return ResponseDto.fail("WRONG DATE", "날짜 선택을 다시 해주세요");
        }

        // 모집 일자가 모집 마감일보다 이전일 경우 에러처리
        if (dDay.isBefore(endDate)) {
            return ResponseDto.fail("WRONG DATE", "날짜 선택을 다시 해주세요");
        }

        post.updateJson(title, address, content, maxNum, startDate, endDate, dDay);

        if (imgFile == null || imgFile.isEmpty()) {
            return ResponseDto.success("업데이트가 완료되었습니다.");
        }

        if (!imgFile.isEmpty()) {
            if (post.getImgUrl().equals(baseImage)) {
                ResponseDto<?> image = amazonS3Service.uploadFile(imgFile, folderName);
                ImageFile imageFile = (ImageFile) image.getData();
                imgUrl = imageFile.getUrl();
                post.updateImgUrl(imgUrl);
            } else {
                ImageFile findImageFile = filesRepository.findByUrl(post.getImgUrl());
                amazonS3Service.removeFile(findImageFile.getImageName(), folderName);
                ResponseDto<?> image = amazonS3Service.uploadFile(imgFile, folderName);
                ImageFile imageFile = (ImageFile) image.getData();
                imgUrl = imageFile.getUrl();
                post.updateImgUrl(imgUrl);
            }
        }


        return ResponseDto.success("업데이트가 완료되었습니다.");
    }

    // 게시글 삭제
    @Transactional
    public ResponseDto<?> deletePost(Long id, HttpServletRequest request) {

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        // 개사굴 유효성 검사e
        Post post = isPresentPost(id);
        if (null == post) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
        }

        if (post.validateMember(member)) {
            return ResponseDto.fail("BAD_REQUEST", "작성자만 삭제할 수 있습니다.");
        }

        postRepository.delete(post);
        return ResponseDto.success("게시글이 삭제되었습니다.");
    }

    // 찜 추가
    @Transactional
    public ResponseDto<?> addWish(Long postId, HttpServletRequest request) {
        ResponseDto<?> responseDto = validation.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Post post = isPresentPost(postId);

        if (null == post) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
        }
        // 검증 로직 -- 이미 좋아요를 누를 경우 중복 좋아요 불가.
        WishList isPresentWish = wishListRepository.findByMemberIdAndPostId(member.getId(), post.getId()).orElse(null);
        if (isPresentWish != null) {
            return ResponseDto.fail("ALREADY LIKE", "이미 좋아요를 누르셨습니다.");
        }


        WishList wishList = WishList.builder()
                .member(member)
                .post(post)
                .build();
        wishListRepository.save(wishList);
        return ResponseDto.success(true);
    }

    // 찜 삭제
    @Transactional
    public ResponseDto<?> removeWish(Long postId, HttpServletRequest request) {
        ResponseDto<?> responseDto = validation.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Post post = isPresentPost(postId);

        if (null == post) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
        }

        WishList isPresentWish = wishListRepository.findByMemberIdAndPostId(member.getId(), post.getId()).orElse(null);
        if (isPresentWish == null) {
            return ResponseDto.fail("NOT FOUND", "찜 목록에 해당 게시글이 없습니다.");
        }
        wishListRepository.deleteByMemberIdAndPostId(member.getId(), post.getId()).orElse(null);


        return ResponseDto.success(false);
    }

    @Transactional(readOnly = true)
    public Post isPresentPost(Long id) {
        Optional<Post> optionalPost = postRepository.findById(id);
        return optionalPost.orElse(null);
    }


}
