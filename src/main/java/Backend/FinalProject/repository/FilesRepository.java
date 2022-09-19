package Backend.FinalProject.repository;

import Backend.FinalProject.domain.ImageFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FilesRepository extends JpaRepository<ImageFile, Long> {

    Optional<ImageFile> findByImageName(String name);

    ImageFile findByUrl(String url);
}
