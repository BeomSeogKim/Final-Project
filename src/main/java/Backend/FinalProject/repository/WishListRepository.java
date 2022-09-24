package Backend.FinalProject.repository;

import Backend.FinalProject.domain.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishListRepository extends JpaRepository<WishList, Long> {

    List<WishList> findAllByMemberId (Long memberId);
}
