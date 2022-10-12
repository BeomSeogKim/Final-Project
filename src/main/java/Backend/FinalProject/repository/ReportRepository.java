package Backend.FinalProject.repository;

import Backend.FinalProject.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAll();

    @Query(value = "select r from Report r where r.memberId is not null")
    List<Report> findByMember();

    @Query(value = "select r from Report r where r.postId is not null ")
    List<Report> findByPost();

    @Query(value = "select r from Report r where r.commentId is not null")
    List<Report> findByComment();

    Report findByMemberIdAndReportMemberId(Long memberId, Long reportMemberId);

    Report findByPostIdAndReportMemberId(Long postId, Long reportMemberId);

    Report findByCommentIdAndReportMemberId(Long commentId, Long reportMemberId);

    @Query("select r from Report r where r.postId =:postId and r.postId is not null ")
    List<Report> findAllByPostId(@Param("postId") Long postId);

    @Query("select r from Report r where r.commentId = :commentId and r.commentId is not null")
    List<Report> findALlByCommentId(@Param("commentId") Long commentId);

    @Query("select r from Report r where r.reportPostId = :reportPostId and r.commentId is not null")
    List<Report> findAllByReportPostId(@Param("reportPostId") Long postId);
}
