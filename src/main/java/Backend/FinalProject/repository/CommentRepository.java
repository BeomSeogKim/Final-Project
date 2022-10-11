package Backend.FinalProject.repository;

import Backend.FinalProject.domain.Comment;
import Backend.FinalProject.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByPost(Post post);

    List<Comment> findAllByPostId(Long postId);
}
