package Backend.FinalProject.Tool;

import Backend.FinalProject.domain.Post;
import Backend.FinalProject.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final PostRepository postRepository;


    /**
     * 매일 오전 1시 기준으로 게시글들 상태 업데이트
     * 모집 마감일이 지난 경우 ==> DONE 상태로 바뀜
     * 모임 일이 지난 경우 ==> CLOSURE 상태로 바뀜
     */

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void run() {
        LocalDate now = LocalDate.now();

        List<Post> postList = postRepository.findAll();
        for (Post post : postList) {
            if (now.isAfter(post.getEndDate())) {
                post.updateStatus();
            }
            if (now.isAfter(post.getDDay())) {
                post.closeStatus();
            }
        }
    }
}
