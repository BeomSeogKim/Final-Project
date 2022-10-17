package Backend.FinalProject.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.domain.*;
import Backend.FinalProject.domain.enums.*;
import Backend.FinalProject.dto.MemberPasswordUpdateDto;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.TokenDto;
import Backend.FinalProject.dto.request.LoginRequestDto;
import Backend.FinalProject.dto.request.MemberUpdateDto;
import Backend.FinalProject.dto.request.SignupRequestDto;
import Backend.FinalProject.repository.*;
import Backend.FinalProject.sercurity.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

import static Backend.FinalProject.domain.SignUpRoot.normal;
import static Backend.FinalProject.domain.enums.AgeCheck.CHECKED;
import static Backend.FinalProject.domain.enums.AgeCheck.UNCHECKED;
import static Backend.FinalProject.domain.enums.Authority.ROLE_MEMBER;
import static Backend.FinalProject.domain.enums.MarketingAgreement.MARKETING_AGREE;
import static Backend.FinalProject.domain.enums.MarketingAgreement.MARKETING_DISAGREE;
import static Backend.FinalProject.domain.enums.Regulation.UNREGULATED;
import static Backend.FinalProject.domain.enums.RequiredAgreement.REQUIRED_AGREE;
import static Backend.FinalProject.domain.enums.RequiredAgreement.REQUIRED_DISAGREE;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final AmazonS3Service amazonS3Service;
    private final FilesRepository fileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SignOutRepository signOutRepository;
    private final PostRepository postRepository;
    private final EntityManager em;

    private final Validation validation;

    // 회원가입 시 기본 이미지 설정이 없을 경우 기본 이미지 설정
    String baseImage = "https://tommy-bucket-final.s3.ap-northeast-2.amazonaws.com/memberImage/6c6c20cf-7490-4d9e-b6f6-73c185a417dd%E1%84%80%E1%85%B5%E1%84%87%E1%85%A9%E1%86%AB%E1%84%8B%E1%85%B5%E1%84%86%E1%85%B5%E1%84%8C%E1%85%B5.webp";
    // S3에 회원 이미지 파일 저장 경로
    String folderName = "/memberImage";


    @Transactional
    public ResponseDto<String> createMember(SignupRequestDto request) {

        String userId = request.getUserId();
        String password = request.getPassword();
        String passwordCheck = request.getPasswordCheck();
        String nickname = request.getNickname();
        MultipartFile imgFile = request.getImgFile();
        String gender = request.getGender();
        Integer age = request.getAge();
        String ageCheck = request.getAgeCheck();
        String requiredAgreement = request.getRequiredAgreement();
        String marketingAgreement = request.getMarketingAgreement();
        String imgUrl;
        int minAge;
        Gender genderSet = Gender.NEUTRAL;
        AgeCheck setAgeCheck = UNCHECKED;
        RequiredAgreement setRequiredAgreement = REQUIRED_DISAGREE;
        MarketingAgreement setMarketingAgreement = MARKETING_DISAGREE;

        // null 값 및 공백이 있는 값 체크하기
        if (userId == null || password == null || nickname == null) {
            log.info("MemberService createMember NULL_DATA");
            return ResponseDto.fail("NULL_DATA", "입력값을 다시 확인해주세요");
        } else if (userId.trim().isEmpty() || password.trim().isEmpty() || nickname.trim().isEmpty()) {
            log.info("MemberService createMember EMPTY_DATA");
            return ResponseDto.fail("EMPTY_DATA", "빈칸을 채워주세요");
        }
        // 비밀번호 및 비밀번호 확인 일치 검사
        if (!password.equals(passwordCheck)) {
            log.info("MemberService createMember DOUBLE-CHECK_ERROR");
            return ResponseDto.fail("DOUBLE-CHECK_ERROR", "두 비밀번호가 일치하지 않습니다");
        }
        // 아이디 중복검사
        if (!isPresentId(userId).isSuccess()) {
            log.info("MemberService createMember ALREADY EXIST-ID");
            return ResponseDto.fail("ALREADY EXIST-ID", "이미 존재하는 아이디 입니다.");
        }
        // 닉네임 중복검사
        if (!isPresentNickname(nickname).isSuccess()) {
            log.info("MemberService createMember ALREADY EXIST-NICKNAME");
            return ResponseDto.fail("ALREADY EXIST-NICKNAME", "이미 존재하는 닉네임 입니다.");
        }
        // 이미지를 업로드 하지 않을 시 기본 이미지 설정
        if (imgFile == null || imgFile.isEmpty()) {
            imgUrl = baseImage;
        } else {
            // 이미지 업로드 관련 로직
            ResponseDto<?> image = amazonS3Service.uploadFile(imgFile, folderName);
            ImageFile imageFile = (ImageFile) image.getData();
            imgUrl = imageFile.getUrl();
        }

        if (requiredAgreement ==null || requiredAgreement.equals("false")) {
            log.info("MemberService createMember NOT ALLOWED");
            return ResponseDto.fail("NOT ALLOWED", "이용약관을 동의해주세요");
        }
        if (ageCheck == null || ageCheck.equals("false")) {
            log.info("MemberService createMember NOT ALLOWED");
            return ResponseDto.fail("NOT ALLOWED", "이용약관을 동의해주세요");
        }
        if (requiredAgreement.equals("true")) {
            setRequiredAgreement = REQUIRED_AGREE;
        }
        if (marketingAgreement == null) {
            setMarketingAgreement = MARKETING_DISAGREE;
        } else if (marketingAgreement.equals("true")) {
            setMarketingAgreement = MARKETING_AGREE;
        }
        if (ageCheck.equals("true")) {
            setAgeCheck = CHECKED;
        }

        if (age < 20) {
            minAge = 10;
        } else if (age < 30) {
            minAge = 20;
        } else if (age < 40) {
            minAge = 30;
        } else if (age < 50) {
            minAge = 40;
        } else {
            minAge = 50;
        }
        if (gender.equals("male")) {
            genderSet = Gender.MALE;
        } else if (gender.equals("female")) {
            genderSet = Gender.FEMALE;
        }

        Member member = Member.builder()
                .userId(userId)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .imgUrl(imgUrl)
                .userRole(ROLE_MEMBER)
                .root(normal)
                .gender(genderSet)
                .minAge(minAge)
                .ageCheck(setAgeCheck)
                .requiredAgreement(setRequiredAgreement)
                .marketingAgreement(setMarketingAgreement)
                .regulation(UNREGULATED)
                .build();

        memberRepository.save(member);
        return ResponseDto.success(member.getUserId() + "님 회원가입 성공");
    }

    @Transactional
    public ResponseDto<String> login(LoginRequestDto loginRequestDto, HttpServletResponse response) throws UnsupportedEncodingException {
        Member member = isPresentMember(loginRequestDto.getUserId());

        if (member == null) {
            log.info("MemberService login ");
            return ResponseDto.fail("", "존재하지 않는 아이디입니다.");
        }
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
            log.info("MemberService login INVALID_PASSWORD");
            return ResponseDto.fail("INVALID_PASSWORD", "잘못된 비밀번호 입니다.");
        }

        byte[] bytes = member.getNickname().getBytes();
        // 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        // 헤더에 토큰 담기
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addHeader("RefreshToken", tokenDto.getRefreshToken());
        response.addHeader("ImgUrl", member.getImgUrl());
        response.addHeader("Id",member.getUserId());
        response.addHeader("nickname", String.valueOf(bytes));
        response.addHeader("role", String.valueOf(member.getUserRole()));

        return ResponseDto.success(member.getUserId() + "님 로그인 성공!");
    }

    @Transactional
    public ResponseDto<?> updateMember(MemberUpdateDto request, HttpServletRequest httpServletRequest,
                                       HttpServletResponse response) {
        String imgUrl;

        String nickname = request.getNickname();
        MultipartFile imgFile = request.getImgFile();

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(httpServletRequest);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();
        Member findMember = memberRepository.findById(member.getId()).get();

        if (nickname == null) {
            log.info("MemberService updateMember NULL_DATA");
            return ResponseDto.fail("NULL_DATA", "입력값을 다시 확인해주세요");
        } else if (nickname.trim().isEmpty()) {
            log.info("MemberService updateMember EMPTY_DATA");
            return ResponseDto.fail("EMPTY_DATA", "빈칸을 채워주세요");
        }

        findMember.updateNickname(nickname);

        // 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(findMember);
        // 헤더에 토큰 담기
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addHeader("RefreshToken", tokenDto.getRefreshToken());
        response.addHeader("ImgUrl", findMember.getImgUrl());
        response.addHeader("Id",findMember.getUserId());

        if (imgFile == null || imgFile.isEmpty()) {
            return ResponseDto.success("업데이트가 완료되었습니다.");
        }

        if (!imgFile.isEmpty()) {
            if (member.getImgUrl().equals(baseImage)) {
                ResponseDto<?> image = amazonS3Service.uploadFile(imgFile, folderName);
                ImageFile imageFile = (ImageFile) image.getData();
                imgUrl = imageFile.getUrl();
                findMember.updateImage(imgUrl);
            } else {
                ImageFile findImageFile = fileRepository.findByUrl(member.getImgUrl());
                amazonS3Service.removeFile(findImageFile.getImageName(), folderName);
                ResponseDto<?> image = amazonS3Service.uploadFile(imgFile, folderName);
                ImageFile imageFile = (ImageFile) image.getData();
                imgUrl = imageFile.getUrl();
                findMember.updateImage(imgUrl);
            }
        }


        return ResponseDto.success("성공적으로 회원 수정이 완료되었습니다");
    }
    @Transactional
    public ResponseDto<?> updateMemberPassword(MemberPasswordUpdateDto request, HttpServletRequest httpServletRequest) {
        String password = request.getPassword();
        String updatePassword = request.getUpdatePassword();
        String UpdatePasswordCheck = request.getUpdatePasswordCheck();

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(httpServletRequest);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();
        Member findMember = memberRepository.findById(member.getId()).get();

        if (!passwordEncoder.matches(password, member.getPassword())) {
            log.info("MemberService updateMemberPassword PASSWORD_ERROR");
            return ResponseDto.fail("PASSWORD_ERROR", "기존 비밀번호가 일치하지 않습니다");
        }

        if (password == null || updatePassword == null || UpdatePasswordCheck == null) {
            log.info("MemberService updateMemberPassword NULL_DATA");
            return ResponseDto.fail("NULL_DATA", "입력값을 다시 확인해주세요");
        } else if (password.trim().isEmpty() || updatePassword.trim().isEmpty() || UpdatePasswordCheck.trim().isEmpty()) {
            log.info("MemberService updateMemberPassword EMPTY_DATA");
            return ResponseDto.fail("EMPTY_DATA", "빈칸을 채워주세요");
        }

        if (!updatePassword.equals(UpdatePasswordCheck)) {
            log.info("MemberService updateMemberPassword DOUBLE-CHECK_ERROR");
            return ResponseDto.fail("DOUBLE-CHECK_ERROR", "두 비밀번호가 일치하지 않습니다");
        }

        findMember.updatePassword(passwordEncoder.encode(updatePassword));

        return ResponseDto.success("비밀번호 수정이 완료되었습니다");
    }

    @Transactional
    public ResponseDto<?> logout(HttpServletRequest request) {

        String refreshToken = request.getHeader("RefreshToken");
        RefreshToken validateToken = refreshTokenRepository.findByKeyValue(refreshToken).orElse(null);

        if (validateToken == null) {
            log.info("MemberService logout ALREADY LOGOUT");
            return ResponseDto.fail("ALREADY LOGOUT", "이미 로그아웃 하셨습니다.");
        }

        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
            log.info("MemberService logout INVALID TOKEN");
            return ResponseDto.fail("INVALID TOKEN", "토큰 값이 올바르지 않습니다.");
        }

        // 맴버객체 찾아오기
        Member member = tokenProvider.getMemberFromAuthentication();
        if (null == member) {
            log.info("MemberService logout MEMBER_NOT_FOUND");
            return ResponseDto.fail("MEMBER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
        }
        tokenProvider.deleteRefreshToken(member);


        return ResponseDto.success("로그아웃 완료");
    }

    @Transactional
    public ResponseDto<?> signOut(HttpServletRequest request) {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();
        Member findMember = memberRepository.findById(member.getId()).orElse(null);
        if (member == null) {
            log.info("MemberService signOut DO NOT EXIST");
            return ResponseDto.fail("DO NOT EXIST", "존재하지 않는 회원입니다.");
        }

        if (!member.getImgUrl().equals(baseImage)) {
            ImageFile deleteImage = fileRepository.findByUrl(member.getImgUrl());
            amazonS3Service.removeFile(deleteImage.getImageName(), folderName);
        }
        // 회원이 주최함 모임들 닫아주기
        List<Post> AllPost = postRepository.findAllByMemberId(member.getId());
        for (Post post : AllPost) {
            if (post.getStatus() == PostState.RECRUIT) {
                post.disclose();
            }
        }


        refreshTokenRepository.deleteById(member.getUserId());

        SignOutMember signOutMember = SignOutMember.builder()
                .userId(member.getUserId())
                .password(member.getPassword())
                .nickname(member.getNickname())
                .minAge(member.getMinAge())
                .imgUrl(member.getImgUrl())
                .regulation(member.getRegulation())
                .build();
        signOutRepository.save(signOutMember);

        member.deleteMember();
        em.merge(member);



        return ResponseDto.success("회원 탈퇴가 성공적으로 수행되었습니다.");
    }


    // 회원 아이디 중복 검사 method
    public ResponseDto<String> isPresentId(String id) {
        Optional<Member> userId = memberRepository.findByUserId(id);
        if (userId.isPresent()) {
            log.info("MemberService isPresentId ALREADY EXIST-ID");
            return ResponseDto.fail("ALREADY EXIST-ID", "이미 존재하는 회원 아이디입니다.");
        } else if (id == null) {
            return ResponseDto.fail("INVALID TYPE", "데이터의 형식을 다시한번 확인해주세요");
        } else {
            return ResponseDto.success("사용 가능한 아이디입니다.");
        }
    }

    // 닉네임 중복 검사 method
    public ResponseDto<String> isPresentNickname(String nickname) {
        Optional<Member> findNickname = memberRepository.findByNickname(nickname);
        if (findNickname.isPresent()) {
            log.info("MemberService isPresentId ALREADY EXIST-NICKNAME");
            return ResponseDto.fail("ALREADY EXIST-NICKNAME", "이미 존재하는 닉네임입니다.");
        } else if (nickname == null) {
            return ResponseDto.fail("INVALID TYPE", "데이터의 형식을 다시한번 확인해주세요");
        } else {
            return ResponseDto.success("사용 가능한 닉네임입니다.");
        }
    }

    // 회원 검색
    @Transactional(readOnly = true)
    public Member isPresentMember(String userId) {
        Optional<Member> findMember = memberRepository.findByUserId(userId);
        return findMember.orElse(null);
    }

    public ResponseDto<?> reissue(HttpServletRequest request, HttpServletResponse response) throws ParseException, ParseException {
        if (tokenProvider.getMemberIdByToken(request.getHeader("Authorization") ) != null) {
            log.info("getMemberIdByToken");
            log.info(tokenProvider.getMemberIdByToken(request.getHeader("Authorization") ));
            return ResponseDto.fail("AVAILABLE CODE","아직 유효한 토큰입니다.");
        }
        if (!tokenProvider.validateToken((request.getHeader("RefreshToken")))) {
            return ResponseDto.fail("INVALID REFRESH TOKEN", "RefreshToken 이 유효하지 않습니다.");
        }
        String memberId = tokenProvider.getMemberFromExpiredAccessToken(request);
        if (null == memberId) {
            return ResponseDto.fail("INCORRECT ACESSTOKEN", "Access Token 값이 유효하지 않습니다.");
        }
        Member member = memberRepository.findByUserId(memberId).orElse(null);

        RefreshToken refreshToken = tokenProvider.isPresentRefreshToken(member);

        if (!refreshToken.getKeyValue().equals(request.getHeader("RefreshToken"))) {
            log.info("refreshToken : "+refreshToken.getKeyValue());
            log.info("header rft : "+request.getHeader("RefreshToken"));
            return ResponseDto.fail("INVALID REFRESH TOKEN","토큰이 일치하지 않습니다.");
        }
        assert member != null;
        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        refreshToken.updateValue(tokenDto.getRefreshToken());
        tokenToHeaders(tokenDto, response);
        return ResponseDto.success("재발급 완료");

    }

    // 헤더에 토큰담기
    public void tokenToHeaders(TokenDto tokenDto, HttpServletResponse response) {
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addHeader("RefreshToken", tokenDto.getRefreshToken());
    }

}
