package Backend.FinalProject.WebSocket.repository;

import Backend.FinalProject.WebSocket.domain.ReadCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReadCheckRepository extends JpaRepository<ReadCheck, Long> {

    List<ReadCheck> findAllByChatMessageId(Long chatMessageId);

}
