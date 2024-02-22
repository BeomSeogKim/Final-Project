package Backend.FinalProject.domain.token;

import Backend.FinalProject.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMember(Member member);
    void deleteById(String id);
    Optional<RefreshToken> findByKeyValue(String refreshToken);
}
