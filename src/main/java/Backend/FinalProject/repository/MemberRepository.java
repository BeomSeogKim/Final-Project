package Backend.FinalProject.repository;

import Backend.FinalProject.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByNickname(String nickName);

    Optional<Member> findByUserId(String user_id);
}
