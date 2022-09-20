package Backend.FinalProject.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageFile extends Timestamped{
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String imageName;

    @Column(length = 1000)
    private String url;

}
