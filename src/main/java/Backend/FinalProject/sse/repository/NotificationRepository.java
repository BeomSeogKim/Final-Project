package Backend.FinalProject.sse.repository;

import Backend.FinalProject.sse.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("select n from Notification n where n.member.id = :userId order by n.id desc")
    List<Notification> findAllByUserId(@Param("userId") Long userId);

    @Query("select count(n) from Notification n where n.member.id = :userId and n.isRead = false")
    Long countUnReadNotifications(@Param("userId") Long userId);

    Optional<Notification> findById(Long NotificationsId);
//    Optional<Notification> findByReceiverId(Long receiverId);

    //    void deleteAllByReceiverId(Long receiverId);
//    void deleteAllByReceiverId(Long receiverId);

    void deleteById(Long notificationId);

}
