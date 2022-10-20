package Backend.FinalProject.service;

import Backend.FinalProject.Tool.Time;
import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.WebSocket.domain.ChatMessage;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import Backend.FinalProject.WebSocket.domain.ReadCheck;
import Backend.FinalProject.WebSocket.repository.ChatMemberRepository;
import Backend.FinalProject.WebSocket.repository.ChatMessageRepository;
import Backend.FinalProject.WebSocket.repository.ChatRoomRepository;
import Backend.FinalProject.domain.*;
import Backend.FinalProject.domain.enums.Category;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.SearchDto;
import Backend.FinalProject.dto.request.post.PostRequestDto;
import Backend.FinalProject.dto.request.post.PostUpdateRequestDto;
import Backend.FinalProject.dto.response.comment.CommentResponseDto;
import Backend.FinalProject.dto.response.post.AllPostResponseDto;
import Backend.FinalProject.dto.response.post.PostResponseDto;
import Backend.FinalProject.dto.response.post.PostResponseDtoPage;
import Backend.FinalProject.repository.CommentRepository;
import Backend.FinalProject.repository.FilesRepository;
import Backend.FinalProject.repository.PostRepository;
import Backend.FinalProject.repository.WishListRepository;
import Backend.FinalProject.sercurity.TokenProvider;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static Backend.FinalProject.Tool.Validation.handleBoolean;
import static Backend.FinalProject.Tool.Validation.handleNull;
import static Backend.FinalProject.domain.QPost.post;
import static Backend.FinalProject.domain.enums.Category.valueOf;
import static Backend.FinalProject.domain.enums.ErrorCode.*;
import static Backend.FinalProject.domain.enums.PostState.*;
import static Backend.FinalProject.domain.enums.Regulation.REGULATED;
import static Backend.FinalProject.domain.enums.Regulation.UNREGULATED;
import static Backend.FinalProject.domain.enums.baseImage.*;
import static java.time.LocalDate.now;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@RestController
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService{

    //== Dependency Injection ==//
    private final EntityManager em;
    private final Validation validation;
    private final TokenProvider tokenProvider;
    private final AmazonS3Service amazonS3Service;
    private final AutomatedChatService automatedChatService;
    private final PostRepository postRepository;
    private final FilesRepository filesRepository;
    private final CommentRepository commentRepository;
    private final WishListRepository wishListRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;

    Time time = new Time();

    /**
     * 게시글 등록
     * @param request : 게시글 작성에 필요한 값
     * @param httpServletRequest : HttpServlet Request
     */
    @Transactional
    public ResponseDto<?> writePost(PostRequestDto request, HttpServletRequest httpServletRequest) {

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(httpServletRequest);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        MultipartFile imgFile = request.getImgFile();

        ResponseDto<Object> nullCheck = checkNullAndEmpty(request);
        if (nullCheck != null) return nullCheck;
//         최대 정원의 수는 최소 3명에서 최대 5명
        ResponseDto<Object> checkRecruitment = handleBoolean(request.getMaxNum() <= 2 || request.getMaxNum() >= 6, POST_OVER_RECRUITMENT);
        if (checkRecruitment!= null) return checkRecruitment;

        // 날짜 String 을 LocalDate 로 변경
        LocalDate startDate, endDate, dDay;
        try {
             startDate = time.stringToLocalDate(request.getStartDate());
             endDate = time.stringToLocalDate(request.getEndDate());
             dDay = time.stringToLocalDate(request.getDDay());
        } catch (Exception e) {
            log.info("PostService createPost INVALID TYPE");
            return ResponseDto.fail("INVALID TYPE", "날짜 형식을 확인해주세요");
        }


        // 모집 시작 날짜, 모집 마감일자, 모집 일자가 현재보다 이전일 경우 에러 처리
        // 모집 마감일이 모집 시작일 보다 이전일 경우 에러처리
        // 모집 일자가 모집 시작일 혹은 모집 마감일 보다 이전일 경우 에러처리
        ResponseDto<Object> checkDate = handleBoolean(
                startDate.isBefore(now()) || endDate.isBefore(now()) || dDay.isBefore(now())
                        || endDate.isBefore(startDate)
                        || dDay.isBefore(startDate) || dDay.isBefore(endDate)
                , POST_WRONG_DATE);
        if (checkDate!=null) return checkDate;


//        String imgUrl = baseImageEtc;
        String imgUrl = uploadImage(request, imgFile);
        Category category = setCategory(request);
        Post post = buildPost(request, member, imgUrl, startDate, endDate, dDay, category);
        postRepository.save(post);

        // 채팅방 생성
        ChatRoom chatRoom = automatedChatService.createChatRoom(post);
        // 방장 채팅방 자동 입장
        ChatMember chatMember = automatedChatService.createChatMember(member, chatRoom);
        // 방장 알림 메세지 자동 기입
        ChatMessage chatMessage = automatedChatService.createChatMessage(member, chatRoom);
        // readCheck 추적
        ReadCheck readCheck = automatedChatService.createReadCheck(chatMember, chatMessage);
        return ResponseDto.success("게시글 작성이 완료되었습니다.");
    }

    /**
     * 게시글 전체 조회
     * @param pageNum : 페이지 번호
     */
    public ResponseDto<?> getPostList(Integer pageNum) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        PageRequest pageRequest = PageRequest.of(pageNum, 9, Sort.by(DESC,"modifiedAt"));
        Page<Post> pageOfPost = postRepository.findAllByOrderByModifiedAtDesc(pageRequest);
        long count = postRepository.findAllHiddenPost(REGULATED, DONE, CLOSURE);
        List<AllPostResponseDto> PostResponseDtoList = new ArrayList<>();

        List<Post> contentOfPost = pageOfPost.getContent();
        makeListOfPostDetail(PostResponseDtoList, contentOfPost);
        return ResponseDto.success(makeListOfTotalPost(pageNum, pageOfPost, count, PostResponseDtoList));
    }

    /**
     * 게시글 상세 조회
     * @param postId : 게시글 아이디
     */
    public ResponseDto<?> getDetailPost(Long postId) {


        Post post = isPresentPost(postId);   // 입력한 id에 해당하는 post 가 있는지 검사 하는 과정
        ResponseDto<Object> checkPost = handleNull(post, POST_NOT_FOUND);
        if (checkPost != null) return checkPost;

        ResponseDto<Object> checkRegulation = handleBoolean(post.getRegulation().equals(REGULATED), POST_REGULATED);
        if (checkRegulation != null) return checkRegulation;

        List<Comment> commentList = commentRepository.findAllByPost(post);
        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();

        getComments(commentList, commentResponseDtoList);

        Member member = tokenProvider.getMemberFromAuthentication();
        List<WishList> peopleList = wishListRepository.findAllByPostId(postId).orElse(null);
        boolean isWish = checkWish(member, peopleList);
        return detailPostInformation(post, commentResponseDtoList, isWish);
    }


    /**
     * 게시글 업데이트
     * @param postId : 게시글 아이디
     * @param postUpdateRequestDto : 업데이트에 필요한 Dto
     * @param httpServletRequest : HttpServlet Request
     */
    @Transactional
    public ResponseDto<?> updatePost(Long postId, PostUpdateRequestDto postUpdateRequestDto, HttpServletRequest httpServletRequest) {

        ResponseDto<?> responseDto = validation.checkAccessToken(httpServletRequest);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();


        Post post = isPresentPost(postId);
        ResponseDto<Object> checkPost = handleNull(post, POST_NOT_FOUND);
        if (checkPost != null) return checkPost;

        ResponseDto<Object> checkRegulation = handleBoolean(post.getRegulation().equals(REGULATED), POST_REGULATED);
        if (checkRegulation != null) return checkRegulation;


        String title = postUpdateRequestDto.getTitle();
        String content = postUpdateRequestDto.getContent();
        String address = postUpdateRequestDto.getAddress();
        String placeX = postUpdateRequestDto.getPlaceX();
        String placeY = postUpdateRequestDto.getPlaceY();
        String placeUrl = postUpdateRequestDto.getPlaceUrl();
        String placeName = postUpdateRequestDto.getPlaceName();
        String detailAddress = postUpdateRequestDto.getDetailAddress();
        int maxNum = postUpdateRequestDto.getMaxNum();
        MultipartFile imgFile = postUpdateRequestDto.getImgFile();
        String imgUrl;

        // 최대 정원은 3명에서 5명
        if (maxNum <= 2 || maxNum >= 6) {
            log.info("PostService updatePost MAXIMUM ERROR");
            return ResponseDto.fail("MAXIMUM ERROR", "모집 정원을 다시 확인해주세요");
        }

        // 날짜 String 을 LocalDate 로 변경
        LocalDate startDate, endDate, dDay;
        try {
            startDate = time.stringToLocalDate(postUpdateRequestDto.getStartDate());
            endDate = time.stringToLocalDate(postUpdateRequestDto.getEndDate());
            dDay = time.stringToLocalDate(postUpdateRequestDto.getDDay());
        } catch (Exception e) {
            log.info("PostService updatePost INVALID TYPE");
            return ResponseDto.fail("INVALID TYPE", "날짜 형식을 확인해주세요");
        }

        // 모집 시작 날짜가 현재보다 이전일 경우 에러 처리
        if (startDate.isBefore(now()) || endDate.isBefore(now()) || dDay.isBefore(now())) {
            log.info("PostService updatePost WRONG DATE");
            return ResponseDto.fail("WRONG DATE", "현재보다 이전 날짜를 택할 수 없습니다.");
        }

        // 모집 마감일자, 모집일이 모집 시작일자보다 이전일 경우 에러처리
        if (endDate.isBefore(startDate) || dDay.isBefore(startDate)) {
            log.info("PostService updatePost WRONG DATE");
            return ResponseDto.fail("WRONG DATE", "날짜 선택을 다시 해주세요");
        }

        // 모집 일자가 모집 마감일보다 이전일 경우 에러처리
        if (dDay.isBefore(endDate)) {
            log.info("PostService updatePost WRONG DATE");
            return ResponseDto.fail("WRONG DATE", "날짜 선택을 다시 해주세요");
        }

        ChatRoom chatRoom = chatRoomRepository.findByPostId(postId).orElse(null);
        assert chatRoom != null;
        chatRoom.updateName(title);

        post.updatePost(title, address, content, maxNum, placeX, placeY, placeUrl, placeName, detailAddress, startDate, endDate, dDay);

        if (imgFile == null || imgFile.isEmpty()) {
            return ResponseDto.success("업데이트가 완료되었습니다.");
        }

        if (!imgFile.isEmpty()) {
//            if (post.getImgUrl().equals(baseImageEtc) || post.getImgUrl().equals(baseImageExercise) || post.getImgUrl().equals(baseImageOnline)||
//                    post.getImgUrl().equals(baseImageStudy) || post.getImgUrl().equals(baseImageReading) || post.getImgUrl().equals(baseImageReligion)
//            || post.getImgUrl().equals(baseImageTravel)) {
            if (post.getImgUrl().equals(ETC.getUrl()) || post.getImgUrl().equals(EXERCISE.getUrl()) || post.getImgUrl().equals(ONLINE.getUrl()) ||
                    post.getImgUrl().equals(STUDY.getUrl()) || post.getImgUrl().equals(READING.getUrl()) || post.getImgUrl().equals(RELIGION.getUrl())
                    || post.getImgUrl().equals(TRAVEL.getUrl())) {
                uploadImage(post, imgFile);
            } else {
                deleteImageAndUpload(post, imgFile);
            }
        }

        return ResponseDto.success("업데이트가 완료되었습니다.");
    }



    // 게시글 삭제

    @Transactional
    public ResponseDto<?> deletePost(Long id, HttpServletRequest request) {

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        // 게시글 유효성 검사
        Post post = isPresentPost(id);
        if (null == post) {
            log.info("PostService deletePost NOT_FOUND");
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
        }
        String role = request.getHeader("role");

        if (!Objects.equals(role, "ROLE_ADMIN")) {
            if (!post.getMember().getUserId().equals(member.getUserId())) {
                log.info("PostService deletePost BAD_REQUEST");
                return ResponseDto.fail("BAD_REQUEST", "작성자만 삭제할 수 있습니다.");
            }
        }

        ChatRoom chatRoom = chatRoomRepository.findByPostId(id).orElse(null);
        assert chatRoom != null;
        chatMessageRepository.deleteAllByChatRoom(chatRoom);
        chatRoomRepository.delete(chatRoom);
        postRepository.delete(post);

        return ResponseDto.success("게시글이 삭제되었습니다.");
    }
    // 찜 추가

    @Transactional
    public ResponseDto<?> addWishList(Long postId, HttpServletRequest request) {
        ResponseDto<?> responseDto = validation.checkAccessToken(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Post post = isPresentPost(postId);

        if (null == post) {
            log.info("PostService addWish NOT_FOUND");
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
        }
        // 검증 로직 -- 이미 좋아요를 누를 경우 중복 좋아요 불가.
        WishList wish = wishListRepository.findByMemberIdAndPostId(member.getId(), post.getId()).orElse(null);
        if (wish != null) {
            log.info("PostService addWish ALREADY LIKE");
            return ResponseDto.fail("ALREADY LIKE", "이미 좋아요를 누르셨습니다.");
        }
        if (post.getMember().getId().equals(member.getId())) {
            return ResponseDto.fail("INVALID ACCESS", "작성자는 좋아요를 누를 수 없습니다");
        }

        System.out.println(post.getMember().getId());
        System.out.println(member.getId());

        WishList wishList = WishList.builder()
                .member(member)
                .post(post)
                .build();
        wishListRepository.save(wishList);
        // 게시글의 numOfWish 에 새로 적용
        post.addWish();
        return ResponseDto.success(true);
    }
    // 찜 삭제

    @Transactional
    public ResponseDto<?> removeWishList(Long postId, HttpServletRequest request) {
        ResponseDto<?> responseDto = validation.checkAccessToken(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Post post = isPresentPost(postId);

        if (null == post) {
            log.info("PostService removeWish NOT_FOUND");
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
        }

        WishList isPresentWish = wishListRepository.findByMemberIdAndPostId(member.getId(), post.getId()).orElse(null);
        if (isPresentWish == null) {
            log.info("PostService removeWish NOT_FOUND");
            return ResponseDto.fail("NOT FOUND", "찜 목록에 해당 게시글이 없습니다.");
        }
        wishListRepository.deleteByMemberIdAndPostId(member.getId(), post.getId()).orElse(null);
        post.removeWish();

        return ResponseDto.success(false);
    }
    @Transactional(readOnly = true)
    public Post isPresentPost(Long id) {
        Optional<Post> optionalPost = postRepository.findById(id);
        return optionalPost.orElse(null);
    }

    public ResponseDto<?> findPost(SearchDto search, Integer pageNum ) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        List<Post> postList = queryFactory.selectFrom(post)
                .where(categoryEq(search.getCategory()), keywordEq(search.getKeyword()), post.status.eq(RECRUIT), post.regulation.eq(UNREGULATED))
                .orderBy(post.modifiedAt.desc())
                .offset(pageNum * 9)
                .limit(9)
                .fetch();
        QueryResults<Post> pageInfo = queryFactory.selectFrom(post)
                .where(categoryEq(search.getCategory()), keywordEq(search.getKeyword()), post.status.eq(RECRUIT), post.regulation.eq(UNREGULATED))
                .orderBy(post.modifiedAt.desc())
                .offset(pageNum * 9)
                .limit(9)
                .fetchResults();

        List<AllPostResponseDto> detailPostInformation = new ArrayList<>();
        for (Post findPost : postList) {

            int numOfComment = commentRepository.findAllCountByPost(findPost);

            if (findPost.getRegulation().equals(UNREGULATED) && findPost.getStatus().equals(RECRUIT)) {
                detailPostInformation.add(
                        AllPostResponseDto.builder()
                                .id(findPost.getId())
                                .title(findPost.getTitle())
                                .maxNum(findPost.getMaxNum())
                                .address(findPost.getAddress())
                                .category(findPost.getCategory())
                                .restDay(time.convertLocalDateToTime((findPost.getEndDate())))
                                .dDay(findPost.getDDay())
                                .imgUrl(findPost.getImgUrl())
                                .status(findPost.getStatus())
                                .authorImgUrl(findPost.getMember().getImgUrl())
                                .authorNickname(findPost.getMember().getNickname())
                                .numOfComment(numOfComment)
                                .numOfWish(findPost.getNumOfWish())
                                .build()
                );
            }
        }
        int totalPage = (int) pageInfo.getTotal() / (int) pageInfo.getLimit();
        int restPage = (int) pageInfo.getTotal() - (int) pageInfo.getLimit() * totalPage;
        if (restPage > 0) {
            totalPage++;
        }
        totalPage = totalPage - 1;      // 전체 페이지 한번 ㅜ줄이기

        boolean firstPage = false;
        boolean nextPage = true;
        boolean previousPage = true;

        if (pageNum == totalPage) {
            nextPage = false;
            firstPage = false;
            previousPage = true;
            if (totalPage == 0) {
                previousPage = false;
            }
        }

        if (pageNum == 0) {
            firstPage = true;
            previousPage = false;
            nextPage = totalPage != 0;
        }

        PostResponseDtoPage informationOfPost = PostResponseDtoPage.builder()
                .postList(detailPostInformation)
                .totalPage(totalPage)
                .currentPage(pageNum)
                .totalPost(pageInfo.getTotal())       // 현재 페이지에 보여야 하는 갯수로 카운트를 진행.
                .isFirstPage(firstPage)
                .hasNextPage(nextPage)
                .hasPreviousPage(previousPage)
                .build();

        return ResponseDto.success(informationOfPost);
    }

    //== Method Line ==//
    private static ResponseDto<Object> checkNullAndEmpty(PostRequestDto request) {
        if (request.getTitle() == null || request.getAddress() == null || request.getContent() == null || request.getMaxNum() == 0 ||
                request.getStartDate() == null || request.getEndDate() == null || request.getDDay() == null ||
                request.getPlaceX() == null || request.getPlaceY() == null || request.getPlaceName() == null
        ) {
            log.info("PostService createPost NULL_DATA");
            return ResponseDto.fail("NULL_DATA", "입력값을 다시 확인해주세요");
        } else if (request.getTitle().trim().isEmpty() || request.getAddress().trim().isEmpty() || request.getContent().trim().isEmpty() ||
                request.getPlaceName().trim().isEmpty()) {
            log.info("PostService createPost EMPTY_DATA");
            return ResponseDto.fail("EMPTY_DATA", "빈칸을 채워주세요");
        }
        return null;
    }

    private String uploadImage(PostRequestDto request, MultipartFile imgFile) {
        String imgUrl = ETC.getUrl();
        // 이미지 업로드 관련 로직
        if (imgFile == null || imgFile.isEmpty()) {
            if (request.getCategory() == null) {
                imgUrl = ETC.getUrl();//baseImageEtc;
            } else if (request.getCategory().equals("exercise")) {
                imgUrl = EXERCISE.getUrl();//baseImageExercise;
            } else if (request.getCategory().equals("travel")) {
                imgUrl = TRAVEL.getUrl();//baseImageTravel;
            } else if (request.getCategory().equals("reading")) {
                imgUrl = READING.getUrl();//baseImageReading;
            } else if (request.getCategory().equals("study")) {
                imgUrl = STUDY.getUrl();//imgUrl = baseImageStudy;
            } else if (request.getCategory().equals("religion")) {
                imgUrl = RELIGION.getUrl(); //baseImageReligion;
            } else if (request.getCategory().equals("online")) {
                imgUrl = ONLINE.getUrl();
            }
        } else {
            ResponseDto<?> image = amazonS3Service.uploadFile(imgFile, "/postImage");
            ImageFile imageFile = (ImageFile) image.getData();
            imgUrl = imageFile.getUrl();
        }
        return imgUrl;
    }
    private static Category setCategory(PostRequestDto request) {
        Category category = Category.ETC;
        if (request.getCategory() == null || request.getCategory().equals("etc")) {
            category = Category.ETC;
        } else if (request.getCategory().equals("exercise")) {
            category = Category.EXERCISE;
        } else if (request.getCategory().equals("travel")) {
            category = Category.TRAVEL;
        } else if (request.getCategory().equals("reading")) {
            category = Category.READING;
        } else if (request.getCategory().equals("study")) {
            category = Category.STUDY;
        } else if (request.getCategory().equals("religion")) {
            category = Category.RELIGION;
        } else if (request.getCategory().equals("online")) {
            category = Category.ONLINE;
        }
        return category;
    }

    private static Post buildPost(PostRequestDto request, Member member, String imgUrl, LocalDate startDate, LocalDate endDate, LocalDate dDay, Category category) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .maxNum(request.getMaxNum())
                .currentNum(0)              // 현재 모집된 정원의 수
                .startDate(startDate)
                .endDate(endDate)
                .imgUrl(imgUrl)
                .status(RECRUIT)      // 현재 모집 중
                .member(member)
                .address(request.getAddress())
                .placeX(request.getPlaceX())
                .placeY(request.getPlaceY())
                .placeUrl(request.getPlaceUrl())
                .placeName(request.getPlaceName())
                .detailAddress(request.getDetailAddress())
                .dDay(dDay)                      // 남은 모집일자
                .category(category)
                .regulation(UNREGULATED)
                .build();
        return post;
    }
    private void makeListOfPostDetail(List<AllPostResponseDto> PostResponseDtoList, List<Post> contentOfPost) {
        for (Post post : contentOfPost) {
            int numOfComment = commentRepository.findAllCountByPost(post);
            if (post.getRegulation().equals(UNREGULATED) && post.getStatus().equals(RECRUIT)) {
                PostResponseDtoList.add(
                        AllPostResponseDto.builder()
                                .id(post.getId())
                                .title(post.getTitle())
                                .maxNum(post.getMaxNum())
                                .address(
                                        post.getDetailAddress().equals("undefined") ? post.getAddress(): post.getAddress() + " " + post.getDetailAddress()
                                )
                                .category(post.getCategory())
                                .restDay(time.convertLocalDateToTime((post.getEndDate())))
                                .dDay(post.getDDay())
                                .imgUrl(post.getImgUrl())
                                .status(post.getStatus())
                                .authorImgUrl(post.getMember().getImgUrl())
                                .authorNickname(post.getMember().getNickname())
                                .numOfComment(numOfComment)
                                .numOfWish(post.getNumOfWish())
                                .build()
                );

            }
        }
    }

    private static PostResponseDtoPage makeListOfTotalPost(Integer pageNum, Page<Post> pageOfPost, long count, List<AllPostResponseDto> PostResponseDtoList) {
        return PostResponseDtoPage.builder()
                .postList(PostResponseDtoList)
                .totalPage(pageOfPost.getTotalPages() - 1)
                .currentPage(pageNum)
                .totalPost(pageOfPost.getTotalElements() - count)       // 현재 페이지에 보여야 하는 갯수로 카운트를 진행.
                .isFirstPage(pageOfPost.isFirst())
                .hasNextPage(pageOfPost.hasNext())
                .hasPreviousPage(pageOfPost.hasPrevious())
                .build();
    }
    private static boolean checkWish(Member member, List<WishList> peopleList) {
        boolean isWish = false;     // 회원이 좋아요를 눌렀는지 안눌렀는지 Check
        if (member != null) {
            for (WishList wishList : peopleList) {
                if (wishList.getMember().getUserId()
                        .equals(member.getUserId())) {
                    isWish = true;
                }
            }
        }
        return isWish;
    }

    private ResponseDto<PostResponseDto> detailPostInformation(Post post, List<CommentResponseDto> commentResponseDtoList, boolean isWish) {
        return ResponseDto.success(PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .authorId(post.getMember().getUserId())
                .authorNickname(post.getMember().getNickname())
                .status(post.getStatus())
                .category(post.getCategory())
                .memberId(post.getMember().getId())
                .memberImgUrl(post.getMember().getImgUrl())
                .address(post.getAddress()).placeX(post.getPlaceX()).placeY(post.getPlaceY()).placeName(post.getPlaceName())
                .placeUrl(post.getPlaceUrl())
                .detailAddress(post.getDetailAddress())
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

    private static void getComments(List<Comment> commentList, List<CommentResponseDto> commentResponseDtoList) {
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
    }



















    private void uploadImage(Post post, MultipartFile imgFile) {
        String imgUrl;
        ResponseDto<?> image = amazonS3Service.uploadFile(imgFile, "/postImage");
        ImageFile imageFile = (ImageFile) image.getData();
        imgUrl = imageFile.getUrl();
        post.updateImg(imgUrl);
    }

    private void deleteImageAndUpload(Post post, MultipartFile imgFile) {
        String imgUrl;
        ImageFile findImageFile = filesRepository.findByUrl(post.getImgUrl());
        amazonS3Service.removeFile(findImageFile.getImageName(), "/postImage");
        ResponseDto<?> image = amazonS3Service.uploadFile(imgFile, "/postImage");
        ImageFile imageFile = (ImageFile) image.getData();
        imgUrl = imageFile.getUrl();
        post.updateImg(imgUrl);
    }

    private Predicate categoryEq(String category) {
        if (category == null || category.trim().isEmpty() || category.equals("ALL")) {
            return null;
        } else {
            return post.category.eq(valueOf(category));
        }
    }

    private Predicate keywordEq(String keyword) {
        return keyword != null ? post.title.contains(keyword).or(post.content.contains(keyword)) : null;
    }
}
