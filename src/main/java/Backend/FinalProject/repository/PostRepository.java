package Backend.FinalProject.repository;

import Backend.FinalProject.domain.Post;
import Backend.FinalProject.domain.enums.PostState;
import Backend.FinalProject.domain.enums.Regulation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findById(Long id);
    List<Post> findAllByMemberId(Long memberId);

    @Query(value = "select p from Post p where p.regulation = :regulation")
    Page<Post> findAllByOrderByModifiedAtDesc(Pageable pageable, @Param("regulation") Regulation regulation);

    @Query(value = "select p from Post p where p.regulation = :regulation")
    Page<Post> findAllByOrderByModifiedAtAsc(Pageable pageable, @Param("regulation") Regulation regulation);

    @Query(value = "select count(p) from Post p where p.regulation = :regulation or p.status =:status or p.status = :status2")
    long findAllHiddenPost(@Param("regulation") Regulation regulation, @Param("status") PostState postState, @Param("status2") PostState postState2);
}
