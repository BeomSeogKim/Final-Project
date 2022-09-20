package Backend.FinalProject.repository;

import Backend.FinalProject.domain.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByPostIdAndMemberId(Long postId, Long memberId);

    Optional<List<Application>> findAllByPostId(Long postId);
}
