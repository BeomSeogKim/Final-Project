package Backend.FinalProject.sse.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor
public class RelatedUrl {

    private static final int MAX_LENGTH = 255;

    @Column(nullable = false, length = MAX_LENGTH)
    private String url;

    public RelatedUrl(String url) throws Exception {
        if (isNotValidRelatedURL(url)) {
            throw new Exception();
        }
        this.url = url;
    }

    private boolean isNotValidRelatedURL(String url) {
        return Objects.isNull(url) || url.length() > MAX_LENGTH || url.isEmpty();
    }
}
