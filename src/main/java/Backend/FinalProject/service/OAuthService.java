package Backend.FinalProject.service;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.UserDetailsImpl;
import Backend.FinalProject.domain.enums.Gender;
import Backend.FinalProject.dto.KakaoUserInfoDto;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.TokenDto;
import Backend.FinalProject.repository.MemberRepository;
import Backend.FinalProject.sercurity.TokenProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static Backend.FinalProject.domain.SignUpRoot.kakao;
import static Backend.FinalProject.domain.enums.Authority.ROLE_MEMBER;
import static Backend.FinalProject.domain.enums.MarketingAgreement.MARKETING_AGREE;
import static Backend.FinalProject.domain.enums.RequiredAgreement.REQUIRED_AGREE;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private static final String BEARER_TYPE = "Bearer";

    @Value("${kakao.clientId}")
    private String clientId;

    @Value("${redirect.uri}")
    private String redirectUri;

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 카카오 로그인
     * @param code
     * @return
     */
    public ResponseDto<?> kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {

        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getAccessToken(code);

        // 2. "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);


        // 3. 카카오 아이디 중복검사
        String kakaoId = kakaoUserInfo.getId();
        Member kakaoUser = memberRepository.findByUserId(kakaoId)
                .orElse(null);

        if (kakaoUser == null) {
            // 회원가입
            String nickname = kakaoUserInfo.getNickname();
            String gender = kakaoUserInfo.getGender();
            Integer minAge = kakaoUserInfo.getMinAge();
            String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
            String imgUrl = kakaoUserInfo.getImgUrl();
            Gender genderSet = Gender.NEUTRAL;
            if (gender.equals("male")) {
                genderSet = Gender.MALE;
            } else if (gender.equals("female")) {
                genderSet = Gender.FEMALE;
            }
            kakaoUser = Member.builder()
                    .userId(kakaoId)
                    .nickname(nickname)
                    .password(encodedPassword)
                    .imgUrl(imgUrl)
                    .gender(genderSet)
                    .minAge(minAge)
                    .userRole(ROLE_MEMBER)
                    .root(kakao)
                    .marketingAgreement(MARKETING_AGREE)
                    .requiredAgreement(REQUIRED_AGREE)
                    .build();

            memberRepository.save(kakaoUser);
        }

        // 4. 강제 로그인 처리
        UserDetails userDetails = new UserDetailsImpl(kakaoUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(kakaoUser);
        // 헤더에 토큰 담기
        response.setContentType("application/json;charset=UTF-8");
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addHeader("RefreshToken", tokenDto.getRefreshToken());
        response.addHeader("ImgUrl", kakaoUser.getImgUrl());
        response.addHeader("Id", kakaoUser.getUserId());
        char[] chars = kakaoUser.getNickname().toCharArray();

        return ResponseDto.success( kakaoUser.getNickname()+ "님 로그인 성공");
    }


    private String getAccessToken(String code) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        System.out.println(jsonNode.toString());
        Long id = jsonNode.get("id").asLong();
        String tempNick = jsonNode.get("properties")
                .get("nickname").toString();
        String imgUrl = jsonNode.get("properties")
                .get("profile_image").asText();
        String tempGender = jsonNode.get("kakao_account")
                .get("gender").toString();
        String age = jsonNode.get("kakao_account")
                .get("age_range").toString();
        String[] representAge = age.split("~");
        String temp = representAge[0];
        String nickname = tempNick.substring(1, tempNick.length() - 1);
        Integer minAge = Integer.valueOf(temp.substring(1,temp.length()));
        String gender = tempGender.substring(1,tempGender.length()-1);
        System.out.println("id " + id + " nickname " + nickname + " imgUrl " + imgUrl + " gender" + gender + " minAge" + minAge);
        return new KakaoUserInfoDto(id.toString(), nickname, imgUrl, gender, minAge);

    }
}
