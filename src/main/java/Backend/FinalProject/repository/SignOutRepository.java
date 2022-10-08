package Backend.FinalProject.repository;

import Backend.FinalProject.domain.SignOutMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignOutRepository extends JpaRepository<SignOutMember, Long> {

}
