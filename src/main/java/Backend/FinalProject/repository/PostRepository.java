package Backend.FinalProject.repository;


import Backend.FinalProject.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository  extends JpaRepository<Post, Long> {
}
