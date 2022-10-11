package Backend.FinalProject.repository;

import Backend.FinalProject.domain.Application;
import Backend.FinalProject.domain.enums.ApplicationState;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByPostIdAndMemberId(Long postId, Long memberId);

    // version1
//    List<Application> findAllByPostId(Long postId);

    // version2
    @Query(value = "select a from Application a left join fetch a.member where a.post.id = :postId")
    List<Application> findAllByPostIdMyAct(@Param("postId") Long postId);

    @Query(value = "select a from Application a where a.post.id = :postId")
    @EntityGraph(attributePaths = {"member", "post"})
    List<Application> findAllByPostIdApplication(@Param("postId") Long postId);

    List<Application> findAllByMemberId(Long memberId);

    List<Application> findAllByMemberIdAndStatus(Long memberId, ApplicationState state);

    Optional<Application> deleteByPostIdAndMemberId(Long postId, Long memberId);

}
