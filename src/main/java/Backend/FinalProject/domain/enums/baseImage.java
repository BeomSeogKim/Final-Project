package Backend.FinalProject.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum baseImage {

    STUDY("https://tommy-bucket-final.s3.ap-northeast-2.amazonaws.com/postImage/%E1%84%80%E1%85%A9%E1%86%BC%E1%84%87%E1%85%AE.webp"),
    ETC("https://tommy-bucket-final.s3.ap-northeast-2.amazonaws.com/postImage/%E1%84%80%E1%85%B5%E1%84%90%E1%85%A1.webp"),
    READING("https://tommy-bucket-final.s3.ap-northeast-2.amazonaws.com/postImage/%E1%84%83%E1%85%A9%E1%86%A8%E1%84%89%E1%85%A5.jpeg"),
    TRAVEL("https://tommy-bucket-final.s3.ap-northeast-2.amazonaws.com/postImage/%E1%84%8B%E1%85%A7%E1%84%92%E1%85%A2%E1%86%BC.jpeg"),
    ONLINE("https://tommy-bucket-final.s3.ap-northeast-2.amazonaws.com/postImage/%E1%84%8B%E1%85%A9%E1%86%AB%E1%84%85%E1%85%A1%E1%84%8B%E1%85%B5%E1%86%AB.webp"),
    EXERCISE("https://tommy-bucket-final.s3.ap-northeast-2.amazonaws.com/postImage/%E1%84%8B%E1%85%AE%E1%86%AB%E1%84%83%E1%85%A9%E1%86%BC.webp"),
    RELIGION("https://tommy-bucket-final.s3.ap-northeast-2.amazonaws.com/postImage/%E1%84%8C%E1%85%A9%E1%86%BC%E1%84%80%E1%85%AD.webp"),

    ;
    private final String url;
}
