package Backend.FinalProject.repository;

import Backend.FinalProject.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAll();

    @Query(value = "select r from Report r where r.memberId is not null")
    List<Report> findByMember();

    @Query(value = "select r from Report r where r.postId is not null ")
    List<Report> findByPost();

    @Query(value = "select r from Report r where r.commentId is not null")
    List<Report> findByComment();
  Report findByMemberId(Long memberId);
  Report findByPostId(Long postId);
  Report findByCommentId(Long commentId);
}
