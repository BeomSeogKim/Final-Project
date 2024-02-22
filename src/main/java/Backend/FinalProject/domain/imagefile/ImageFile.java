package Backend.FinalProject.domain.imagefile;

import Backend.FinalProject.domain.Timestamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageFile extends Timestamped {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String imageName;

    @Column(length = 1000)
    private String url;
}
