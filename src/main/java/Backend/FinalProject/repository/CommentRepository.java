package Backend.FinalProject.repository;

import Backend.FinalProject.domain.Comment;
import Backend.FinalProject.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // version 1
    // List<Comment> findAllByPost(Post post);

    // version 2
    // 댓글 조회시 댓글 작성자도 같이 조회
    @Query(value = "select c from Comment c left join fetch c.member")
    List<Comment> findAllByPost(Post post);
    List<Comment> findAllByPostId(Long postId);
}
