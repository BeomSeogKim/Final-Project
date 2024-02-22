package Backend.FinalProject.domain.comment;

import Backend.FinalProject.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // version 1
    // List<Comment> findAllByPost(Post post);

    // version 2
    // 댓글 조회시 댓글 작성자도 같이 조회
    @Query(value = "select c from Comment c left join fetch c.member")
    List<Comment> findAllByPost(Post post);

    @Query(value = "select count(c) from Comment c where c.post = :post")
    int  findAllCountByPost(@Param("post") Post post);
    List<Comment> findAllByPostId(Long postId);
}
