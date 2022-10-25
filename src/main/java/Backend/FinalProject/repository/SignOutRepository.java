package Backend.FinalProject.repository;

import Backend.FinalProject.domain.SignOutMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignOutRepository extends JpaRepository<SignOutMember, Long> {

    Optional<SignOutMember> findByUserId(String userId);

}
