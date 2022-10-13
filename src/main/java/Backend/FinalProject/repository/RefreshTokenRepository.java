package Backend.FinalProject.repository;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMember(Member member);
    void deleteById(String id);
    Optional<RefreshToken> findByKeyValue(String refreshToken);
}
