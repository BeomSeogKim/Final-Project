package Backend.FinalProject.repository;

import Backend.FinalProject.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAll();
    @Query(value = "select * from Report where member_id is not null",nativeQuery = true)
    List<Report> findByMember();

    @Query(value = "select * from Report where post_id is not null",nativeQuery = true)
    List<Report> findByPost();

    @Query(value = "select * from Report where comment_id is not null",nativeQuery = true)
    List<Report> findBycomment();
}
