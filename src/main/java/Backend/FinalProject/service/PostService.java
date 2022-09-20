package Backend.FinalProject.service;

import Backend.FinalProject.domain.ImageFile;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import Backend.FinalProject.dto.PostResponseDto;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.PostRequestDto;
import Backend.FinalProject.repository.PostRepository;
import Backend.FinalProject.sercurity.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    private final TokenProvider tokenProvider;

    private final AmazonS3Service amazonS3Service;

    String folderName = "/postImage";

    @Transactional  // 게시글 등록
    public ResponseDto<?> createPost(PostRequestDto requestDto, MultipartFile imgFile, HttpServletRequest httpServletRequest) {

        Member member = validateMember(httpServletRequest);

        String title = requestDto.getTitle();
        String address = requestDto.getAddress();
        String content = requestDto.getContent();
        int maxNum = requestDto.getMaxNum();
        LocalDateTime startDate = requestDto.getStartDate();
        LocalDateTime endDate = requestDto.getEndDate();
        String imgPost;

        if (title == null || address == null || content == null ||  startDate == null || endDate == null ) {
            return ResponseDto.fail("NULL_DATA", "입력값을 다시 확인해주세요");
        } else if (title.trim().isEmpty() || address.trim().isEmpty() || content.trim().isEmpty() ) {
            return ResponseDto.fail("EMPTY_DATA", "빈칸을 채워주세요");
        }

        ResponseDto<?> image = amazonS3Service.uploadFile(imgFile, folderName);
        ImageFile imageFile = (ImageFile) image.getData();
        imgPost = imageFile.getUrl();

        Post post = Post.builder()
                .member(member)
                .title(title)
                .address(address)
                .content(content)
                .maxNum(maxNum)
                .startDate(startDate)
                .endDate(endDate)
                .imgPost(imgPost)
                .build();

        postRepository.save(post);


        return ResponseDto.success(post.getTitle());
    }

    public ResponseDto<?> getAllPost() {    // 게시글 전체 조회

        List<Post> all = postRepository.findAll();
        List<PostResponseDto> PostResponseDtoList = new ArrayList<>();
        for (Post post : all) {
            PostResponseDtoList.add(
                    PostResponseDto.builder()
                            .title(post.getTitle())
                            .address(post.getAddress())
                            .maxNum(post.getMaxNum())
                            .build()
            );

        }
        return ResponseDto.success("SUCCESS");
    }

    public ResponseDto<?> getPost(Long id){     // 게시글 상세 조회
        Post post = isPresentPost(id);

        return ResponseDto.success(PostResponseDto.builder()
                .title(post.getTitle())
                .address(post.getAddress())
                .content(post.getContent())
                .maxNum(post.getMaxNum())
                .imgPost(post.getImgPost())
                .startDate(post.getStartDate())
                .endDate(post.getEndDate())
                .build()
        );

    }

    @Transactional  // 게시글 업데이트
    public ResponseDto<?> updatePost(Long id, HttpServletRequest httpServletRequest){

        return null;

    }
    @Transactional   // 게시글 삭제
    public ResponseDto<?> deletePost(Long id, HttpServletRequest httpServletRequest){

        return null;
    }


    public Member validateMember(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
            return null;
        }
        return tokenProvider.getMemberFromAuthentication();
    }

    @Transactional(readOnly = true)
    public Post isPresentPost(Long id) {
        Optional<Post> optionalPost = postRepository.findById(id);
        return optionalPost.orElse(null);
    }

}
