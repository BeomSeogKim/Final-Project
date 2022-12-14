package Backend.FinalProject.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.domain.Comment;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.comment.CommentRequestDto;
import Backend.FinalProject.dto.response.comment.AllCommentResponseDto;
import Backend.FinalProject.repository.CommentRepository;
import Backend.FinalProject.repository.PostRepository;
import Backend.FinalProject.sse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static Backend.FinalProject.domain.enums.Regulation.UNREGULATED;
import static Backend.FinalProject.sse.domain.NotificationType.REPLY;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final Validation validation;
    private final NotificationService notificationService;


    public ResponseDto<?> getCommentList(Long postId) {
        List<Comment> commentList = commentRepository.findAllByPostId(postId);
        List<AllCommentResponseDto> commentResponseDto = new ArrayList<>();
        for (Comment comment : commentList) {
            if (comment.getRegulation().equals(UNREGULATED)) {
                commentResponseDto.add(
                        AllCommentResponseDto.builder()
                                .commentId(comment.getId())
                                .memberImage(comment.getMember().getImgUrl())
                                .memberNickname(comment.getMember().getNickname())
                                .content(comment.getContent())
                                .memberId(comment.getMember().getUserId())
                                .commentMemberId(comment.getMember().getId())
                                .build()
                );
            }
        }

        return ResponseDto.success(commentResponseDto);
    }


    // ?????? ??????
    public ResponseDto<?> writeComment(Long postId, CommentRequestDto commentRequestDto, HttpServletRequest request) throws Exception {
        // ?????? ????????? ??????
        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Optional<Post> optionalPost = postRepository.findById(postId);
        Post post = optionalPost.orElse(null);
        if (post == null) {
            log.info("CommentService writeComment INVALID POST_ID");
            return ResponseDto.fail("INVALID POST_ID", "????????? ?????? ????????? ?????????.");
        }

        String commentDto = commentRequestDto.getComment();
        if (commentDto == null) {
            log.info("CommentService writeComment EMPTY COMMENT");
            return ResponseDto.fail("EMPTY COMMENT", "????????? ??????????????????");
        }

        Comment comment = Comment.builder()
                .content(commentDto)
                .member(member)
                .post(post)
                .regulation(UNREGULATED)
                .build();

        commentRepository.save(comment);
        //TODO
        String url = "https://3355.world/detail/" + post.getId();
        if (post.getMember() != member) {
            notificationService.send(post.getMember(), REPLY, member.getNickname()+ "?????? " + post.getTitle() + "???????????? ????????? ???????????????.", url);
        }
        return ResponseDto.success("?????? ????????? ?????????????????????.");

    }

    // ?????? ??????
    public ResponseDto<?> editComment(Long commentId, CommentRequestDto commentRequestDto, HttpServletRequest request) {
        // ?????? ????????? ??????
        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        // ????????? ??????
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        Comment comment = optionalComment.orElse(null);
        if (comment == null) {
            log.info("CommentService editComment NOT FOUND");
            return ResponseDto.fail("NOT FOUND", "?????? ????????? ?????? ??? ????????????.");
        }
        if (!comment.getMember().getId().equals(member.getId())) {
            log.info("CommentService editComment NO AUTHORITY");
            return ResponseDto.fail("NO AUTHORITY", "???????????? ????????? ???????????????.");
        }

        String commentDto = commentRequestDto.getComment();
        if (commentDto == null || commentDto.isEmpty()) {
            log.info("CommentService editComment EMPTY COMMENT");
            return ResponseDto.fail("EMPTY COMMENT", "????????? ??????????????????");
        }

        comment.editComment(commentDto);

        return ResponseDto.success("?????? ????????? ?????????????????????.");

    }

    // ?????? ??????
    public ResponseDto<?> deleteComment(Long commentId, HttpServletRequest request) {

        // ?????? ????????? ??????
        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        // ????????? ??????
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        Comment comment = optionalComment.orElse(null);
        if (comment == null) {
            log.info("CommentService deleteComment NOT FOUND");
            return ResponseDto.fail("NOT FOUND", "?????? ????????? ?????? ??? ????????????.");
        }
        String role = request.getHeader("role");
        if (!Objects.equals(role, "ROLE_ADMIN")) {
            if (!Objects.equals(comment.getMember().getId(), member.getId())) {
                log.info("CommentService deleteComment NO AUTHORITY");
                return ResponseDto.fail("NO AUTHORITY", "???????????? ????????? ???????????????.");
            }
        }


        commentRepository.delete(comment);
        return ResponseDto.success("?????? ????????? ?????????????????????.");
    }
}
