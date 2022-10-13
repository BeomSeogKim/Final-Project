package Backend.FinalProject.Tool;

import Backend.FinalProject.domain.Post;
import Backend.FinalProject.domain.SignOutMember;
import Backend.FinalProject.repository.PostRepository;
import Backend.FinalProject.repository.SignOutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final PostRepository postRepository;
    private final SignOutRepository signOutRepository;


    /**
     * 매일 오전 1시 기준으로 게시글들 상태 업데이트
     * 모집 마감일이 지났는데 모집 인원이 0이 아닌 게시글의 경우 ==> DONE 상태로 바꿈
     */

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void run() {
        LocalDate now = LocalDate.now();

        List<Post> postList = postRepository.findAll();
        for (Post post : postList) {
            if (now.isAfter(post.getEndDate()) && post.getCurrentNum() != 0) {
                post.updateStatus();
            }
            if (now.isAfter(post.getEndDate()) && post.getCurrentNum() == 0) {
                post.closeStatus();
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void deleteInformation() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime criticalLimit = now.minusDays(7);
        System.out.println("criticalLimit = " + criticalLimit);

        List<SignOutMember> signOutMemberList = signOutRepository.findAll();
        System.out.println("signOutMemberList = " + signOutMemberList);
        for (SignOutMember signOutMember : signOutMemberList) {
            if (criticalLimit.isAfter(signOutMember.getCreatedAt())) {
                signOutRepository.delete(signOutMember);
            }
        }
    }
}
