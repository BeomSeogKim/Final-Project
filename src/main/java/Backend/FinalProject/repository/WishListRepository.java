package Backend.FinalProject.repository;

import Backend.FinalProject.domain.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishListRepository extends JpaRepository<WishList, Long> {

    Optional<List<WishList>> findAllByMemberId (Long memberId);

    Optional<WishList> findByMemberIdAndPostId(Long memberId, Long PostId);
}
