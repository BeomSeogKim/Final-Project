package Backend.FinalProject.repository;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PostRepository extends JpaRepository<Post, Long> {


    Optional<Post> findById(Long id);


    List<Post> findAllById(Member member);
}
